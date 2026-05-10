package com.qs.biblioteca.service;

import com.qs.biblioteca.dto.*;
import com.qs.biblioteca.repository.LivroRepository;
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

    private static final String MSG_USUARIO_NAO_ENCONTRADO = "Usuário não encontrado";
    private static final String CAMPO_LOGRADOURO = "logradouro";
    private static final String CAMPO_BAIRRO = "bairro";
    private static final String CAMPO_CIDADE = "cidade";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LivroRepository livroRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LivroRepository livroRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.livroRepository = livroRepository;
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_USUARIO_NAO_ENCONTRADO));
        return new UsuarioResponse(usuario);
    }

    public UsuarioResponse atualizarPorEmail(String email, Map<String, Object> dados) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_USUARIO_NAO_ENCONTRADO));

        if (dados.containsKey("nome")) {
            usuario.setNome((String) dados.get("nome"));
        }
        if (dados.containsKey("nickname")) {
            usuario.setNickname((String) dados.get("nickname"));
        }
        atualizarListasUsuario(usuario, dados);
        atualizarEnderecosUsuario(usuario, dados);
        if (dados.containsKey("bio")) usuario.setBio((String) dados.get("bio"));
        aplicarMetaLeitura(usuario, dados);
        if (dados.containsKey("avatarBase64")) usuario.setAvatarBase64((String) dados.get("avatarBase64"));
        if (dados.containsKey("bgBase64")) usuario.setBgBase64((String) dados.get("bgBase64"));
        aplicarPerfilPublico(usuario, dados);
        if (dados.containsKey("cep")) usuario.setCep((String) dados.get("cep"));
        if (dados.containsKey(CAMPO_LOGRADOURO)) usuario.setLogradouro((String) dados.get(CAMPO_LOGRADOURO));
        if (dados.containsKey(CAMPO_BAIRRO)) usuario.setBairro((String) dados.get(CAMPO_BAIRRO));
        if (dados.containsKey(CAMPO_CIDADE)) usuario.setCidade((String) dados.get(CAMPO_CIDADE));
        if (dados.containsKey("uf")) usuario.setUf((String) dados.get("uf"));

        usuarioRepository.save(usuario);
        return new UsuarioResponse(usuario);
    }

    private void aplicarMetaLeitura(Usuario usuario, Map<String, Object> dados) {
        if (!dados.containsKey("metaLeitura")) return;
        Object m = dados.get("metaLeitura");
        if (m instanceof Number number) usuario.setMetaLeitura(number.intValue());
    }

    private void aplicarPerfilPublico(Usuario usuario, Map<String, Object> dados) {
        if (!dados.containsKey("perfilPublico")) return;
        Object p = dados.get("perfilPublico");
        if (p instanceof Boolean b) usuario.setPerfilPublico(b);
    }

    public List<PublicUsuarioResponse> listarLeitoresPublicos(String emailAtual) {
        return usuarioRepository.findAll()
                .stream()
                .filter(u -> !u.getEmail().equalsIgnoreCase(emailAtual))
                .limit(20)
                .map(PublicUsuarioResponse::new)
                .toList();
    }

    public PublicUsuarioResponse buscarPublicoPorNickname(String nickname) {
        Usuario usuario = usuarioRepository.findByNicknameIgnoreCase(nickname)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_USUARIO_NAO_ENCONTRADO));
        return new PublicUsuarioResponse(usuario);
    }

    public PerfilPublicoDetalhadoResponse buscarPerfilPublicoDetalhado(String nickname) {
        Usuario usuario = usuarioRepository.findByNicknameIgnoreCase(nickname)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_USUARIO_NAO_ENCONTRADO));
        if (!usuario.isPerfilPublico()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este perfil é privado");
        }
        return new PerfilPublicoDetalhadoResponse(usuario, livroRepository.findByUserEmail(usuario.getEmail()));
    }

    public List<PublicUsuarioResponse> buscarPublicoPorTermo(String termo) {
        String limpo = termo.trim();
        if (limpo.startsWith("@")) {
            return usuarioRepository.findByNicknameIgnoreCase(limpo.substring(1))
                    .map(u -> List.of(new PublicUsuarioResponse(u)))
                    .orElse(List.of());
        }
        List<Usuario> porNome = usuarioRepository.findByNomeContainingIgnoreCase(limpo);
        if (!porNome.isEmpty()) {
            return porNome.stream().map(PublicUsuarioResponse::new).toList();
        }
        return usuarioRepository.findByNicknameIgnoreCase(limpo)
                .map(u -> List.of(new PublicUsuarioResponse(u)))
                .orElse(List.of());
    }

    public void redefinirSenha(String email, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual incorreta");
        }
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    public void excluirPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, MSG_USUARIO_NAO_ENCONTRADO));
        usuarioRepository.delete(usuario);
    }

    @SuppressWarnings("unchecked")
    private void atualizarListasUsuario(Usuario usuario, Map<String, Object> dados) {
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
    }

    @SuppressWarnings("unchecked")
    private void atualizarEnderecosUsuario(Usuario usuario, Map<String, Object> dados) {
        if (!dados.containsKey("enderecos")) return;
        Object e = dados.get("enderecos");
        if (!(e instanceof List)) return;
        List<Map<String, String>> lista = (List<Map<String, String>>) e;
        List<Usuario.Endereco> enderecos = new ArrayList<>();
        for (Map<String, String> m : lista) {
            Usuario.Endereco end = new Usuario.Endereco();
            end.setCep(m.get("cep"));
            end.setLogradouro(m.get(CAMPO_LOGRADOURO));
            end.setNumero(m.get("numero"));
            end.setComplemento(m.get("complemento"));
            end.setBairro(m.get(CAMPO_BAIRRO));
            end.setCidade(m.get(CAMPO_CIDADE));
            end.setUf(m.get("uf"));
            enderecos.add(end);
        }
        usuario.setEnderecos(enderecos);
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
