package com.qs.biblioteca.e2e;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.AvaliacaoRepository;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E – AvaliacaoController")
class AvaliacaoE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String baseUrl;
    private String jwtToken;
    private String jwtToken2;
    private Livro livroSalvo;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;

        avaliacaoRepository.deleteAll();
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();

        jwtToken = registrarEObterToken("leitor@email.com", "senha123");
        jwtToken2 = registrarEObterToken("outro@email.com", "senha456");
        livroSalvo = criarLivroViaApi("Harry Potter", "J.K. Rowling");
    }

    @Test
    @DisplayName("Deve criar avaliação com sucesso")
    void criarAvaliacao() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId(), "ID do livro não pode ser nulo");
        String url = baseUrl + "/api/avaliacoes/livro/" + idLivro;

        String payload = """
                {
                  "rating": 5,
                  "comentario": "Excelente livro!"
                }
                """;

        HttpEntity<String> request = new HttpEntity<>(payload, headersComJwt(jwtToken));

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Excelente livro!"));
    }

    @Test
    @DisplayName("Deve listar avaliações do livro")
    void listarAvaliacoes() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        String payload = "{\"rating\": 4, \"comentario\": \"Muito bom\"}";
        restTemplate.exchange(urlPost, HttpMethod.POST, new HttpEntity<>(payload, headersComJwt(jwtToken)), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/livro/" + idLivro,
                HttpMethod.GET,
                new HttpEntity<>(headersComJwt(jwtToken)),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Muito bom"));
    }

    @Test
    @DisplayName("Deve excluir avaliação")
    void excluirAvaliacao() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        String payload = "{\"rating\": 3, \"comentario\": \"Ok\"}";
        restTemplate.exchange(urlPost, HttpMethod.POST, new HttpEntity<>(payload, headersComJwt(jwtToken)), String.class);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/livro/" + idLivro,
                HttpMethod.DELETE,
                new HttpEntity<>(headersComJwt(jwtToken)),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
    }

    @Test
    @DisplayName("Deve curtir e descurtir avaliação de outro usuário")
    void curtirEDescurtirAvaliacao() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        String payload = "{\"rating\": 5, \"comentario\": \"Ótimo!\"}";
        ResponseEntity<String> avaliacaoRes = restTemplate.exchange(
                urlPost, HttpMethod.POST, new HttpEntity<>(payload, headersComJwt(jwtToken)), String.class
        );
        assertEquals(HttpStatus.OK, avaliacaoRes.getStatusCode());

        String avaliacaoBody = Objects.requireNonNull(avaliacaoRes.getBody());
        String avaliacaoId = extractId(avaliacaoBody);

        ResponseEntity<String> curtirRes = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/" + avaliacaoId + "/curtir",
                HttpMethod.POST,
                new HttpEntity<>(headersComJwt(jwtToken2)),
                String.class
        );

        assertEquals(HttpStatus.OK, curtirRes.getStatusCode());
        String curtirBody = Objects.requireNonNull(curtirRes.getBody());
        assertTrue(curtirBody.contains("\"totalCurtidas\":1") || curtirBody.contains("\"totalCurtidas\" : 1"));

        ResponseEntity<String> descurtirRes = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/" + avaliacaoId + "/curtir",
                HttpMethod.POST,
                new HttpEntity<>(headersComJwt(jwtToken2)),
                String.class
        );

        assertEquals(HttpStatus.OK, descurtirRes.getStatusCode());
        String descurtirBody = Objects.requireNonNull(descurtirRes.getBody());
        assertTrue(descurtirBody.contains("\"totalCurtidas\":0") || descurtirBody.contains("\"totalCurtidas\" : 0"));
    }

    @Test
    @DisplayName("Deve retornar avaliações do usuário logado com título e autor do livro")
    void listarMinhas() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        String payload = "{\"rating\": 5, \"comentario\": \"Favorito de todos os tempos\"}";
        restTemplate.exchange(urlPost, HttpMethod.POST, new HttpEntity<>(payload, headersComJwt(jwtToken)), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/minhas",
                HttpMethod.GET,
                new HttpEntity<>(headersComJwt(jwtToken)),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = Objects.requireNonNull(response.getBody());
        assertTrue(body.contains("Favorito de todos os tempos"));
        assertTrue(body.contains("Harry Potter"));
        assertTrue(body.contains("J.K. Rowling"));
        assertTrue(body.contains("livroId"), "Resposta deve conter livroId");
        assertTrue(body.contains("livroCover"), "Resposta deve conter livroCover");
    }

    @Test
    @DisplayName("Deve responder a uma avaliação e excluir a resposta")
    void responderEExcluirResposta() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        String payload = "{\"rating\": 4, \"comentario\": \"Legal\"}";
        ResponseEntity<String> avaliacaoRes = restTemplate.exchange(
                urlPost, HttpMethod.POST, new HttpEntity<>(payload, headersComJwt(jwtToken)), String.class
        );
        String avaliacaoId = extractId(Objects.requireNonNull(avaliacaoRes.getBody()));

        String respostaPayload = "{\"texto\": \"Concordo totalmente!\"}";
        ResponseEntity<String> respostaRes = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/" + avaliacaoId + "/responder",
                HttpMethod.POST,
                new HttpEntity<>(respostaPayload, headersComJwt(jwtToken2)),
                String.class
        );

        assertEquals(HttpStatus.OK, respostaRes.getStatusCode());
        String respostaBody = Objects.requireNonNull(respostaRes.getBody());
        assertTrue(respostaBody.contains("Concordo totalmente!"));

        String respostaId = extractRespostaId(respostaBody);

        ResponseEntity<Void> deleteRespostaRes = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/" + avaliacaoId + "/resposta/" + respostaId,
                HttpMethod.DELETE,
                new HttpEntity<>(headersComJwt(jwtToken2)),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteRespostaRes.getStatusCode());
    }

    @Test
    @DisplayName("Nao deve curtir a propria avaliacao — deve retornar 400")
    void curtir_propriaAvaliacao_deveRetornar400() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        ResponseEntity<String> avaliacaoRes = restTemplate.exchange(
                urlPost, HttpMethod.POST,
                new HttpEntity<>("{\"rating\": 5, \"comentario\": \"Meu comentario\"}", headersComJwt(jwtToken)),
                String.class
        );
        String avaliacaoId = extractId(Objects.requireNonNull(avaliacaoRes.getBody()));

        String url = baseUrl + "/api/avaliacoes/" + avaliacaoId + "/curtir";
        HttpEntity<Void> entity = new HttpEntity<>(headersComJwt(jwtToken));
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    @DisplayName("Nao deve excluir resposta de outro usuario — deve retornar 403")
    void excluirResposta_deOutroUsuario_deveRetornar403() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        ResponseEntity<String> avaliacaoRes = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/livro/" + idLivro, HttpMethod.POST,
                new HttpEntity<>("{\"rating\": 4, \"comentario\": \"Comentario\"}", headersComJwt(jwtToken)),
                String.class
        );
        String avaliacaoId = extractId(Objects.requireNonNull(avaliacaoRes.getBody()));

        ResponseEntity<String> respostaRes = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/" + avaliacaoId + "/responder", HttpMethod.POST,
                new HttpEntity<>("{\"texto\": \"Resposta do usuario2\"}", headersComJwt(jwtToken2)),
                String.class
        );
        String respostaId = extractRespostaId(Objects.requireNonNull(respostaRes.getBody()));

        String url = baseUrl + "/api/avaliacoes/" + avaliacaoId + "/resposta/" + respostaId;
        HttpEntity<Void> entity = new HttpEntity<>(headersComJwt(jwtToken));
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    private String extractId(String json) {
        int idx = json.indexOf("\"id\":\"");
        if (idx < 0) idx = json.indexOf("\"id\" : \"");
        int start = json.indexOf('"', idx + 5) + 1;
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private String extractRespostaId(String json) {
        int respostasIdx = json.indexOf("\"respostas\"");
        if (respostasIdx < 0) return "";
        int idx = json.indexOf("\"id\":\"", respostasIdx);
        if (idx < 0) idx = json.indexOf("\"id\" : \"", respostasIdx);
        int start = json.indexOf('"', idx + 5) + 1;
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    private String registrarEObterToken(String email, String senha) {
        RegisterRequest reg = new RegisterRequest();
        reg.setNome("Leitor Teste");
        reg.setEmail(email);
        reg.setSenha(senha);
        reg.setCep("01310100");
        reg.setLogradouro("Av. Paulista");
        reg.setBairro("Bela Vista");
        reg.setCidade("Sao Paulo");
        reg.setUf("SP");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                reg,
                AuthResponse.class
        );

        AuthResponse body = Objects.requireNonNull(response.getBody(), "Resposta de registro nula");
        return Objects.requireNonNull(body.getToken(), "Token não gerado");
    }

    private Livro criarLivroViaApi(String titulo, String autor) {
        Livro livro = new Livro();
        livro.setTitle(titulo);
        livro.setAuthor(autor);
        livro.setStatus("LIDO");
        livro.setPages(300);
        livro.setCover("https://example.com/capa.jpg");
        livro.setExcerpt("Sinopse");
        livro.setLanguage("Portugues");
        livro.setPublisher("Editora");
        livro.setPublishedDate("2024-01-01");

        HttpEntity<Livro> request = new HttpEntity<>(livro, headersComJwt(jwtToken));

        ResponseEntity<Livro> response = restTemplate.exchange(
                baseUrl + "/api/livros",
                HttpMethod.POST,
                request,
                Livro.class
        );

        return Objects.requireNonNull(response.getBody(), "Erro ao criar livro via API");
    }

    @Test
    @DisplayName("Deve retornar lista de medias de avaliacoes com livroId e livroCover")
    void mediasDeveRetornarLista() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        restTemplate.exchange(urlPost, HttpMethod.POST,
                new HttpEntity<>("{\"rating\": 5, \"comentario\": \"Media test\"}", headersComJwt(jwtToken)), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/medias",
                HttpMethod.GET,
                new HttpEntity<>(headersComJwt(jwtToken)),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = Objects.requireNonNull(response.getBody());
        assertTrue(body.contains("Harry Potter"), "Deve conter título do livro");
        assertTrue(body.contains("livroId"), "Deve conter o ID do livro para navegação");
        assertTrue(body.contains("livroCover"), "Deve conter a capa do livro");
    }

    @Test
    @DisplayName("Deve retornar outros leitores que avaliaram o mesmo livro")
    void outrosLeitoresDeveRetornarLista() {
        String idLivro = Objects.requireNonNull(livroSalvo.getId());
        String urlPost = baseUrl + "/api/avaliacoes/livro/" + idLivro;
        restTemplate.exchange(urlPost, HttpMethod.POST,
                new HttpEntity<>("{\"rating\": 4, \"comentario\": \"Leitor 1\"}", headersComJwt(jwtToken)), String.class);
        restTemplate.exchange(urlPost, HttpMethod.POST,
                new HttpEntity<>("{\"rating\": 3, \"comentario\": \"Leitor 2\"}", headersComJwt(jwtToken2)), String.class);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/avaliacoes/livro/" + idLivro + "/leitores",
                HttpMethod.GET,
                new HttpEntity<>(headersComJwt(jwtToken)),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    private HttpHeaders headersComJwt(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!token.isEmpty()) {
            headers.setBearerAuth(token);
        }
        return headers;
    }
}
