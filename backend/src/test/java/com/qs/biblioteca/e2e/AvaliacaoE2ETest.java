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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E – AvaliacaoController (sistema de avaliações)")
class AvaliacaoE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired private AvaliacaoRepository avaliacaoRepository;
    @Autowired private LivroRepository livroRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private String baseUrl;
    private String jwtToken;
    private Livro livroSalvo;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
        avaliacaoRepository.deleteAll();
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();

        jwtToken = registrarEObterToken("leitor@email.com", "senha123");
        livroSalvo = criarLivroViaApi("Harry Potter", "J.K. Rowling");
    }


    @Nested
    @DisplayName("POST /api/avaliacoes/livro/{livroId}")
    class CriarAvaliacaoTests {

        @Test
        @DisplayName("Deve criar avaliacao e retornar 200 com dados salvos")
        void criar_comDadosValidos_deveRetornar200() {
            var payload = Map.of("rating", 5, "comentario", "Excelente livro!");

            ResponseEntity<String> response = restTemplate.exchange(
                    avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("5"));
            assertTrue(response.getBody().contains("Excelente livro!"));
        }

        @Test
        @DisplayName("Deve criar avaliacao sem comentario e retornar 200")
        void criar_semComentario_deveRetornar200() {
            var payload = Map.of("rating", 3);

            ResponseEntity<String> response = restTemplate.exchange(
                    avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, avaliacaoRepository.count());
        }

        @Test
        @DisplayName("Deve atualizar avaliacao existente ao reavaliar o mesmo livro")
        void criar_segundaVez_deveAtualizarExistente() {
            var primeira = Map.of("rating", 2, "comentario", "Regular");
            restTemplate.exchange(avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.POST, new HttpEntity<>(primeira, headersComJwt()), String.class);

            var segunda = Map.of("rating", 5, "comentario", "Melhorou muito!");
            ResponseEntity<String> response = restTemplate.exchange(
                    avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.POST,
                    new HttpEntity<>(segunda, headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, avaliacaoRepository.count(), "Deve existir apenas 1 avaliacao por usuario/livro");
            assertTrue(response.getBody().contains("5"));
        }

        @Test
        @DisplayName("Deve retornar 400 para rating menor que 1")
        void criar_ratingZero_deveRetornar400() {
            var payload = Map.of("rating", 0);
            var url = avaliacoesUrl(livroSalvo.getId());
            var req = new HttpEntity<>(payload, headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(url, HttpMethod.POST, req, String.class));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 400 para rating maior que 5")
        void criar_ratingSeis_deveRetornar400() {
            var payload = Map.of("rating", 6);
            var url = avaliacoesUrl(livroSalvo.getId());
            var req = new HttpEntity<>(payload, headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(url, HttpMethod.POST, req, String.class));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 400 quando rating estiver ausente")
        void criar_semRating_deveRetornar400() {
            var payload = Map.of("comentario", "Sem nota");
            var url = avaliacoesUrl(livroSalvo.getId());
            var req = new HttpEntity<>(payload, headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(url, HttpMethod.POST, req, String.class));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 404 para livro inexistente")
        void criar_livroInexistente_deveRetornar404() {
            var payload = Map.of("rating", 4);
            var url = avaliacoesUrl("id-inexistente");
            var req = new HttpEntity<>(payload, headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(url, HttpMethod.POST, req, String.class));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 403 ao avaliar sem JWT")
        void criar_semJwt_deveRetornar403() {
            var payload = Map.of("rating", 4);
            var url = avaliacoesUrl(livroSalvo.getId());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.postForEntity(url, payload, String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }


    @Nested
    @DisplayName("GET /api/avaliacoes/livro/{livroId}")
    class ListarAvaliacoesTests {

        @Test
        @DisplayName("Deve listar avaliacoes do livro e retornar 200")
        void listar_comAvaliacaoExistente_deveRetornar200ComLista() {
            var payload = Map.of("rating", 5, "comentario", "Otimo!");
            restTemplate.exchange(avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.POST, new HttpEntity<>(payload, headersComJwt()), String.class);

            ResponseEntity<String> response = restTemplate.exchange(
                    avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("Otimo!"));
        }

        @Test
        @DisplayName("Deve retornar lista vazia para livro sem avaliacoes")
        void listar_semAvaliacoes_deveRetornarListaVazia() {
            ResponseEntity<String> response = restTemplate.exchange(
                    avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("[]", response.getBody().trim());
        }

        @Test
        @DisplayName("Deve retornar 404 para livro inexistente")
        void listar_livroInexistente_deveRetornar404() {
            var url = avaliacoesUrl("id-invalido");
            var req = new HttpEntity<>(headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(url, HttpMethod.GET, req, String.class));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 403 sem JWT")
        void listar_semJwt_deveRetornar403() {
            var url = avaliacoesUrl(livroSalvo.getId());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(url, String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }


    @Nested
    @DisplayName("DELETE /api/avaliacoes/livro/{livroId}")
    class ExcluirAvaliacaoTests {

        @Test
        @DisplayName("Deve excluir avaliacao existente e retornar 204")
        void excluir_avaliacaoExistente_deveRetornar204() {
            var payload = Map.of("rating", 4);
            restTemplate.exchange(avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.POST, new HttpEntity<>(payload, headersComJwt()), String.class);

            ResponseEntity<Void> response = restTemplate.exchange(
                    avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.DELETE,
                    new HttpEntity<>(headersComJwt()),
                    Void.class);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertEquals(0, avaliacaoRepository.count());
        }

        @Test
        @DisplayName("Deve retornar 404 ao excluir avaliacao inexistente")
        void excluir_semAvaliacao_deveRetornar404() {
            var url = avaliacoesUrl(livroSalvo.getId());
            var req = new HttpEntity<>(headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(url, HttpMethod.DELETE, req, String.class));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 404 ao excluir livro inexistente")
        void excluir_livroInexistente_deveRetornar404() {
            var url = avaliacoesUrl("id-invalido");
            var req = new HttpEntity<>(headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(url, HttpMethod.DELETE, req, String.class));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }


    @Nested
    @DisplayName("GET /api/avaliacoes/medias")
    class MediasTests {

        @Test
        @DisplayName("Deve retornar lista vazia de medias quando nao ha avaliacoes")
        void medias_semAvaliacoes_deveRetornarListaVazia() {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/avaliacoes/medias",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("[]", response.getBody().trim());
        }

        @Test
        @DisplayName("Deve calcular media corretamente apos avaliacao")
        void medias_comAvaliacao_deveCalcularMedia() {
            var payload = Map.of("rating", 4);
            restTemplate.exchange(avaliacoesUrl(livroSalvo.getId()),
                    HttpMethod.POST, new HttpEntity<>(payload, headersComJwt()), String.class);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/avaliacoes/medias",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("4.0"));
            assertTrue(response.getBody().contains("Harry Potter"));
        }

        @Test
        @DisplayName("Deve retornar 403 sem JWT")
        void medias_semJwt_deveRetornar403() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(baseUrl + "/api/avaliacoes/medias", String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }


    @Nested
    @DisplayName("GET /api/avaliacoes/livro/{livroId}/leitores")
    class OutrosLeitoresTests {

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha outros leitores")
        void leitores_semOutrosLeitores_deveRetornarListaVazia() {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/avaliacoes/livro/" + livroSalvo.getId() + "/leitores",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("[]", response.getBody().trim());
        }

        @Test
        @DisplayName("Deve retornar 404 para livro inexistente")
        void leitores_livroInexistente_deveRetornar404() {
            var req = new HttpEntity<>(headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(baseUrl + "/api/avaliacoes/livro/id-invalido/leitores",
                            HttpMethod.GET, req, String.class));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 403 sem JWT")
        void leitores_semJwt_deveRetornar403() {
            var url = baseUrl + "/api/avaliacoes/livro/" + livroSalvo.getId() + "/leitores";
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(url, String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }


    private String avaliacoesUrl(String livroId) {
        return baseUrl + "/api/avaliacoes/livro/" + livroId;
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
                baseUrl + "/api/auth/register", reg, AuthResponse.class);

        assertNotNull(response.getBody());
        return response.getBody().getToken();
    }

    private Livro criarLivroViaApi(String titulo, String autor) {
        Livro livro = new Livro();
        livro.setTitle(titulo);
        livro.setAuthor(autor);
        livro.setStatus("LIDO");
        livro.setPages(300);
        livro.setCover("https://example.com/capa.jpg");
        livro.setExcerpt("Sinopse de " + titulo);
        livro.setLanguage("Portugues");
        livro.setPublisher("Editora");
        livro.setPublishedDate("2024-01-01");

        ResponseEntity<Livro> response = restTemplate.exchange(
                baseUrl + "/api/livros",
                HttpMethod.POST,
                new HttpEntity<>(livro, headersComJwt()),
                Livro.class);

        assertNotNull(response.getBody());
        return response.getBody();
    }

    private HttpHeaders headersComJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        return headers;
    }
}
