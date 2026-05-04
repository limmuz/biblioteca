package com.qs.biblioteca.service;

import com.qs.biblioteca.dto.*;
import com.qs.biblioteca.model.Role;
import com.qs.biblioteca.model.Usuario;
import com.qs.biblioteca.repository.UsuarioRepository;
import com.qs.biblioteca.security.JwtService;
import com.qs.biblioteca.validator.UsuarioValidator;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse registrar(RegisterRequest request) {

        UsuarioValidator.validarRegistro(request);

        if (usuarioRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail().toLowerCase());
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
        usuario.setRole(Role.USER);
        usuario.setCep(request.getCep());
        usuario.setLogradouro(request.getLogradouro());
        usuario.setBairro(request.getBairro());
        usuario.setCidade(request.getCidade());
        usuario.setUf(request.getUf());

        Usuario salvo = usuarioRepository.save(usuario);

        return gerarAuthResponse(salvo);
    }

    public AuthResponse autenticar(AuthRequest request) {

        if (request.getEmail() == null || request.getSenha() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email e senha são obrigatórios");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        return gerarAuthResponse(usuario);
    }

    public UsuarioResponse buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        return new UsuarioResponse(usuario);
    }

    @SuppressWarnings("unchecked")
    public UsuarioResponse atualizarPorEmail(String email, Map<String, Object> dados) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (dados.containsKey("nome")) {
            usuario.setNome((String) dados.get("nome"));
        }
        if (dados.containsKey("nickname")) {
            usuario.setNickname((String) dados.get("nickname"));
        }
        if (dados.containsKey("telefones")) {
            Object t = dados.get("telefones");
            if (t instanceof List) {
                usuario.setTelefones((List<String>) t);
            }
        }
        if (dados.containsKey("redesSociais")) {
            Object r = dados.get("redesSociais");
            if (r instanceof List) {
                usuario.setRedesSociais((List<String>) r);
            }
        }
        if (dados.containsKey("enderecos")) {
            Object e = dados.get("enderecos");
            if (e instanceof List) {
                List<Map<String, String>> lista = (List<Map<String, String>>) e;
                List<Usuario.Endereco> enderecos = new ArrayList<>();
                for (Map<String, String> m : lista) {
                    Usuario.Endereco end = new Usuario.Endereco();
                    end.setCep(m.get("cep"));
                    end.setLogradouro(m.get("logradouro"));
                    end.setNumero(m.get("numero"));
                    end.setComplemento(m.get("complemento"));
                    end.setBairro(m.get("bairro"));
                    end.setCidade(m.get("cidade"));
                    end.setUf(m.get("uf"));
                    enderecos.add(end);
                }
                usuario.setEnderecos(enderecos);
            }
        }
        // Endereço legado
        if (dados.containsKey("cep")) usuario.setCep((String) dados.get("cep"));
        if (dados.containsKey("logradouro")) usuario.setLogradouro((String) dados.get("logradouro"));
        if (dados.containsKey("bairro")) usuario.setBairro((String) dados.get("bairro"));
        if (dados.containsKey("cidade")) usuario.setCidade((String) dados.get("cidade"));
        if (dados.containsKey("uf")) usuario.setUf((String) dados.get("uf"));

        usuarioRepository.save(usuario);
        return new UsuarioResponse(usuario);
    }

    public void excluirPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        usuarioRepository.delete(usuario);
    }

    private AuthResponse gerarAuthResponse(Usuario usuario) {
        String token = jwtService.generateToken(
                usuario.getEmail(),
                Map.of(
                        "nome", usuario.getNome(),
                        "role", usuario.getRole().name()));

        return new AuthResponse(token, usuario.getNome(), usuario.getEmail());
    }
}
