package com.qs.biblioteca.e2e;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.AvaliacaoRepository;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.NotificacaoRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E – NotificacaoController (/api/notificacoes)")
@SuppressWarnings("null")
class NotificacaoE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    private String baseUrl;
    private String tokenSeguidor;
    private String tokenAtor;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api";
        notificacaoRepository.deleteAll();
        avaliacaoRepository.deleteAll();
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();

        tokenSeguidor = registrarEObterToken("seguidor@email.com", "senha123");
        tokenAtor = registrarEObterToken("ator@email.com", "senha456");

        String atorId = obterIdDoUsuario(tokenAtor);
        seguirUsuario(tokenSeguidor, atorId);

        Livro livro = criarLivroViaApi(tokenAtor, "O Senhor dos Anéis", "Tolkien");
        avaliarLivro(tokenAtor, livro.getId());
    }

    @Nested
    @DisplayName("GET /api/notificacoes")
    class ListarNotificacoesTests {

        @Test
        @DisplayName("Deve retornar notificações do seguidor quando o ator avalia um livro")
        void listar_comNotificacao_deveRetornarLista() {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    baseUrl + "/notificacoes",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt(tokenSeguidor)),
                    Object[].class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Object[] body = Objects.requireNonNull(response.getBody());
            assertTrue(body.length > 0, "O seguidor deve ter ao menos uma notificação");
        }

        @Test
        @DisplayName("Deve retornar 401 ao listar sem token")
        void listar_semToken_deveRetornar401() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(baseUrl + "/notificacoes", String.class)
            );
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("GET /api/notificacoes/contagem")
    class ContagemTests {

        @Test
        @DisplayName("Deve retornar contagem de notificações não lidas")
        void contagem_comNotificacoesNaoLidas_deveRetornarMaiorQueZero() {
            ResponseEntity<Map<String, Long>> response = restTemplate.exchange(
                    baseUrl + "/notificacoes/contagem",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt(tokenSeguidor)),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Long>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Long> body = Objects.requireNonNull(response.getBody());
            assertTrue(body.containsKey("naoLidas"));
            assertTrue(body.get("naoLidas") > 0, "Deve haver notificações não lidas");
        }

        @Test
        @DisplayName("Deve retornar 401 ao consultar contagem sem token")
        void contagem_semToken_deveRetornar401() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(baseUrl + "/notificacoes/contagem", String.class)
            );
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("PUT /api/notificacoes/marcar-lidas")
    class MarcarLidasTests {

        @Test
        @DisplayName("Deve marcar todas as notificações como lidas e retornar 204")
        void marcarLidas_deveRetornar204EZerarContagem() {
            ResponseEntity<Void> marcarRes = restTemplate.exchange(
                    baseUrl + "/notificacoes/marcar-lidas",
                    HttpMethod.PUT,
                    new HttpEntity<>(headersComJwt(tokenSeguidor)),
                    Void.class
            );

            assertEquals(HttpStatus.NO_CONTENT, marcarRes.getStatusCode());

            ResponseEntity<Map<String, Long>> contagemRes = restTemplate.exchange(
                    baseUrl + "/notificacoes/contagem",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt(tokenSeguidor)),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Long>>() {}
            );

            assertEquals(HttpStatus.OK, contagemRes.getStatusCode());
            Map<String, Long> body = Objects.requireNonNull(contagemRes.getBody());
            assertEquals(0L, body.get("naoLidas"), "Após marcar como lidas, contagem deve ser zero");
        }

        @Test
        @DisplayName("Deve retornar 401 ao marcar como lidas sem token")
        void marcarLidas_semToken_deveRetornar401() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(
                            baseUrl + "/notificacoes/marcar-lidas",
                            HttpMethod.PUT,
                            HttpEntity.EMPTY,
                            Void.class
                    )
            );
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }
    }

    private String registrarEObterToken(String email, String senha) {
        RegisterRequest reg = new RegisterRequest();
        reg.setNome("Usuario Teste");
        reg.setEmail(email);
        reg.setSenha(senha);
        reg.setCep("01310100");
        reg.setLogradouro("Av. Paulista");
        reg.setBairro("Bela Vista");
        reg.setCidade("Sao Paulo");
        reg.setUf("SP");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/auth/register", reg, AuthResponse.class);
        AuthResponse body = Objects.requireNonNull(response.getBody(), "AuthResponse não pode ser nulo");
        return Objects.requireNonNull(body.getToken(), "Token não pode ser nulo");
    }

    private String obterIdDoUsuario(String token) {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/usuarios/me",
                HttpMethod.GET,
                new HttpEntity<>(headersComJwt(token)),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
        );
        return (String) Objects.requireNonNull(response.getBody()).get("id");
    }

    private void seguirUsuario(String tokenSeguidor, String atorId) {
        String payload = "{\"leitoresSeguidos\": [\"" + atorId + "\"]}";
        HttpHeaders headers = headersComJwt(tokenSeguidor);
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                baseUrl + "/usuarios/me",
                HttpMethod.PUT,
                new HttpEntity<>(payload, headers),
                Void.class
        );
    }

    private Livro criarLivroViaApi(String token, String titulo, String autor) {
        Livro livro = new Livro();
        livro.setTitle(titulo);
        livro.setAuthor(autor);
        livro.setStatus("LIDO");
        livro.setPages(500);
        livro.setCover("https://example.com/capa.jpg");
        livro.setExcerpt("Sinopse");
        livro.setLanguage("Portugues");
        livro.setPublisher("Editora");
        livro.setPublishedDate("2001-01-01");

        ResponseEntity<Livro> response = restTemplate.exchange(
                baseUrl + "/livros",
                HttpMethod.POST,
                new HttpEntity<>(livro, headersComJwt(token)),
                Livro.class
        );
        return Objects.requireNonNull(response.getBody(), "Livro não pode ser nulo");
    }

    private void avaliarLivro(String token, String livroId) {
        String payload = "{\"rating\": 5, \"comentario\": \"Obra-prima!\"}";
        HttpHeaders headers = headersComJwt(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                baseUrl + "/avaliacoes/livro/" + livroId,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                String.class
        );
    }

    private HttpHeaders headersComJwt(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
}
