package com.qs.biblioteca.service;

import com.qs.biblioteca.dto.AuthRequest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.PerfilPublicoDetalhadoResponse;
import com.qs.biblioteca.dto.PublicUsuarioResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.dto.UsuarioResponse;
import com.qs.biblioteca.model.Role;
import com.qs.biblioteca.model.Usuario;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import com.qs.biblioteca.security.JwtService;
import com.qs.biblioteca.validator.UsuarioValidator;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LivroRepository livroRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.livroRepository = livroRepository;
    }

    public AuthResponse registrar(RegisterRequest request) {

        UsuarioValidator.validarRegistro(request);

        String email = Objects.requireNonNull(request.getEmail()).toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email já cadastrado"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNome(
                Objects.requireNonNull(request.getNome())
        );

        usuario.setEmail(email);

        usuario.setSenhaHash(
                passwordEncoder.encode(
                        Objects.requireNonNull(request.getSenha())
                )
        );

        usuario.setRole(Role.USER);

        usuario.setCep(request.getCep());
        usuario.setLogradouro(request.getLogradouro());
        usuario.setBairro(request.getBairro());
        usuario.setCidade(request.getCidade());
        usuario.setUf(request.getUf());

        Usuario salvo = Objects.requireNonNull(
                usuarioRepository.save(usuario)
        );

        return gerarAuthResponse(salvo);
    }

    public AuthResponse autenticar(AuthRequest request) {

        if (request.getEmail() == null || request.getSenha() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email e senha são obrigatórios"
            );
        }

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findByEmailLeve(
                        request.getEmail().toLowerCase()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciais inválidas"
                ))
        );

        if (!passwordEncoder.matches(
                request.getSenha(),
                usuario.getSenhaHash()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciais inválidas"
            );
        }

        return gerarAuthResponse(usuario);
    }

    public UsuarioResponse buscarPorEmail(String email) {

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findByEmail(
                        Objects.requireNonNull(email).toLowerCase()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_USUARIO_NAO_ENCONTRADO
                ))
        );

        return new UsuarioResponse(usuario);
    }

    public UsuarioResponse atualizarPorEmail(
            String email,
            Map<String, Object> dados
    ) {

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findByEmail(
                        Objects.requireNonNull(email).toLowerCase()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_USUARIO_NAO_ENCONTRADO
                ))
        );

        if (dados.containsKey("nome")) {
            usuario.setNome((String) dados.get("nome"));
        }

        if (dados.containsKey("nickname")) {
            usuario.setNickname((String) dados.get("nickname"));
        }

        atualizarListasUsuario(usuario, dados);
        atualizarEnderecosUsuario(usuario, dados);

        if (dados.containsKey("bio")) {
            usuario.setBio((String) dados.get("bio"));
        }

        aplicarMetaLeitura(usuario, dados);

        if (dados.containsKey("avatarBase64")) {
            usuario.setAvatarBase64((String) dados.get("avatarBase64"));
        }

        if (dados.containsKey("bgBase64")) {
            usuario.setBgBase64((String) dados.get("bgBase64"));
        }

        aplicarPerfilPublico(usuario, dados);

        if (dados.containsKey("cep")) {
            usuario.setCep((String) dados.get("cep"));
        }

        if (dados.containsKey(CAMPO_LOGRADOURO)) {
            usuario.setLogradouro((String) dados.get(CAMPO_LOGRADOURO));
        }

        if (dados.containsKey(CAMPO_BAIRRO)) {
            usuario.setBairro((String) dados.get(CAMPO_BAIRRO));
        }

        if (dados.containsKey(CAMPO_CIDADE)) {
            usuario.setCidade((String) dados.get(CAMPO_CIDADE));
        }

        if (dados.containsKey("uf")) {
            usuario.setUf((String) dados.get("uf"));
        }

        usuarioRepository.save(usuario);

        return new UsuarioResponse(usuario);
    }

    private void aplicarMetaLeitura(
            @NonNull Usuario usuario,
            Map<String, Object> dados
    ) {

        if (!dados.containsKey("metaLeitura")) {
            return;
        }

        Object meta = dados.get("metaLeitura");

        if (meta instanceof Number number) {
            usuario.setMetaLeitura(number.intValue());
        }
    }

    private void aplicarPerfilPublico(
            @NonNull Usuario usuario,
            Map<String, Object> dados
    ) {

        if (!dados.containsKey("perfilPublico")) {
            return;
        }

        Object perfil = dados.get("perfilPublico");

        if (perfil instanceof Boolean b) {
            usuario.setPerfilPublico(b);
        }
    }

    public List<PublicUsuarioResponse> listarLeitoresPublicos(
            String emailAtual
    ) {

        return usuarioRepository.findPublicosLeves(
                        Objects.requireNonNull(emailAtual).toLowerCase()
                )
                .stream()
                .limit(20)
                .map(u -> new PublicUsuarioResponse(
                        Objects.requireNonNull(u)
                ))
                .toList();
    }

    public PublicUsuarioResponse buscarPublicoPorNickname(
            String nickname
    ) {

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findByNicknameIgnoreCase(
                        Objects.requireNonNull(nickname)
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_USUARIO_NAO_ENCONTRADO
                ))
        );

        return new PublicUsuarioResponse(usuario);
    }

    public PerfilPublicoDetalhadoResponse buscarPerfilPublicoDetalhado(
            String nickname
    ) {

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findByNicknameIgnoreCase(
                        Objects.requireNonNull(nickname)
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_USUARIO_NAO_ENCONTRADO
                ))
        );

        if (!usuario.isPerfilPublico()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Este perfil é privado"
            );
        }

        return new PerfilPublicoDetalhadoResponse(
                usuario,
                livroRepository.findByUserEmail(usuario.getEmail())
        );
    }

    public List<PublicUsuarioResponse> buscarPublicoPorTermo(
            String termo
    ) {

        String limpo = Objects.requireNonNull(termo).trim();

        if (limpo.startsWith("@")) {

            return usuarioRepository
                    .findByNicknameLeve(limpo.substring(1))
                    .stream()
                    .limit(1)
                    .map(u -> new PublicUsuarioResponse(
                            Objects.requireNonNull(u)
                    ))
                    .toList();
        }

        List<Usuario> porNome = usuarioRepository
                .findByNomeLeve(limpo);

        if (!porNome.isEmpty()) {

            return porNome.stream()
                    .map(u -> new PublicUsuarioResponse(
                            Objects.requireNonNull(u)
                    ))
                    .toList();
        }

        return usuarioRepository
                .findByNicknameLeve(limpo)
                .stream()
                .limit(1)
                .map(u -> new PublicUsuarioResponse(
                        Objects.requireNonNull(u)
                ))
                .toList();
    }

    public PerfilPublicoDetalhadoResponse buscarPerfilPublicoDetalhadoPorId(
            String id
    ) {

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findById(
                        Objects.requireNonNull(id)
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_USUARIO_NAO_ENCONTRADO
                ))
        );

        if (!usuario.isPerfilPublico()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Este perfil é privado"
            );
        }

        return new PerfilPublicoDetalhadoResponse(
                usuario,
                livroRepository.findByUserEmail(usuario.getEmail())
        );
    }

    public void redefinirSenha(
            String email,
            String senhaAtual,
            String novaSenha
    ) {

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findByEmail(
                        Objects.requireNonNull(email).toLowerCase()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Email não encontrado"
                ))
        );

        if (!passwordEncoder.matches(
                Objects.requireNonNull(senhaAtual),
                usuario.getSenhaHash()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Senha atual incorreta"
            );
        }

        usuario.setSenhaHash(
                passwordEncoder.encode(
                        Objects.requireNonNull(novaSenha)
                )
        );

        usuarioRepository.save(usuario);
    }

    public void excluirPorEmail(String email) {

        Usuario usuario = Objects.requireNonNull(
                usuarioRepository.findByEmail(
                        Objects.requireNonNull(email).toLowerCase()
                ).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        MSG_USUARIO_NAO_ENCONTRADO
                ))
        );

        usuarioRepository.delete(usuario);
    }

    @SuppressWarnings("unchecked")
    private void atualizarListasUsuario(
            @NonNull Usuario usuario,
            Map<String, Object> dados
    ) {

        if (dados.containsKey("telefones")) {

            Object telefones = dados.get("telefones");

            if (telefones instanceof List<?>) {
                usuario.setTelefones((List<String>) telefones);
            }
        }

        if (dados.containsKey("redesSociais")) {

            Object redes = dados.get("redesSociais");

            if (redes instanceof List<?>) {
                usuario.setRedesSociais((List<String>) redes);
            }
        }

        if (dados.containsKey("leitoresSeguidos")) {

            Object seguidos = dados.get("leitoresSeguidos");

            if (seguidos instanceof List<?>) {
                usuario.setLeitoresSeguidos((List<String>) seguidos);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void atualizarEnderecosUsuario(
            @NonNull Usuario usuario,
            Map<String, Object> dados
    ) {

        if (!dados.containsKey("enderecos")) {
            return;
        }

        Object enderecosObj = dados.get("enderecos");

        if (!(enderecosObj instanceof List<?> listaObj)) {
            return;
        }

        List<Usuario.Endereco> enderecos = new ArrayList<>();

        for (Object item : listaObj) {

            if (!(item instanceof Map<?, ?> mapaObj)) {
                continue;
            }

            Map<String, String> mapa = (Map<String, String>) mapaObj;

            Usuario.Endereco endereco = new Usuario.Endereco();

            endereco.setCep(mapa.get("cep"));
            endereco.setLogradouro(mapa.get(CAMPO_LOGRADOURO));
            endereco.setNumero(mapa.get("numero"));
            endereco.setComplemento(mapa.get("complemento"));
            endereco.setBairro(mapa.get(CAMPO_BAIRRO));
            endereco.setCidade(mapa.get(CAMPO_CIDADE));
            endereco.setUf(mapa.get("uf"));

            enderecos.add(endereco);
        }

        usuario.setEnderecos(enderecos);
    }

    private AuthResponse gerarAuthResponse(
            @NonNull Usuario usuario
    ) {

        String token = jwtService.generateToken(
                usuario.getEmail(),
                Map.of(
                        "nome", usuario.getNome(),
                        "role", usuario.getRole().name()
                )
        );

        return new AuthResponse(
                token,
                usuario.getNome(),
                usuario.getEmail()
        );
    }
}