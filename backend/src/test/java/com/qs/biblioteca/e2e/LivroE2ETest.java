package com.qs.biblioteca.e2e;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E – LivroController (CRUD completo com JWT)")
@SuppressWarnings("null")
class LivroE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String baseUrl;
    private String livrosUrl;
    private String jwtToken;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
        livrosUrl = baseUrl + "/api/livros";
        livroRepository.deleteAll();
        usuarioRepository.deleteAll();
        jwtToken = registrarEObterToken("teste@email.com", "senha123");
    }

    @Nested
    @DisplayName("POST /api/livros")
    class CriarLivroTests {

        @Test
        @DisplayName("Deve criar livro e retornar 201 com o livro salvo")
        void criar_comDadosValidos_deveRetornar201() {
            ResponseEntity<Livro> response = restTemplate.exchange(
                    livrosUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Dom Casmurro", "Machado de Assis"), headersComJwt()),
                    Livro.class
            );

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            Livro body = Objects.requireNonNull(response.getBody(), "Response body não pode ser null");
            assertNotNull(body.getId());
            assertEquals("Dom Casmurro", body.getTitle());
            assertEquals("Machado de Assis", body.getAuthor());
            assertEquals("QUERO LER", body.getStatus());
        }
    }

    @Nested
    @DisplayName("GET /api/livros")
    class ListarLivrosTests {

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha livros cadastrados")
        void listar_semLivros_deveRetornarListaVazia() {
            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl, HttpMethod.GET, new HttpEntity<>(headersComJwt()), Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(0, Objects.requireNonNull(response.getBody()).length);
        }

        @Test
        @DisplayName("Deve listar somente os livros do usuario autenticado")
        void listar_comLivros_deveRetornarLista() {
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Livro A", "Autor A"), headersComJwt()), Livro.class);
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Livro B", "Autor B"), headersComJwt()), Livro.class);

            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl, HttpMethod.GET, new HttpEntity<>(headersComJwt()), Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(2, Objects.requireNonNull(response.getBody()).length);
        }

        @Test
        @DisplayName("Deve filtrar livros pelo parametro search")
        void listar_comSearch_deveRetornarFiltrado() {
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Harry Potter", "Rowling"), headersComJwt()), Livro.class);
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Dom Casmurro", "Machado"), headersComJwt()), Livro.class);

            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl + "?search=Harry", HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()), Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Livro[] body = Objects.requireNonNull(response.getBody());
            assertEquals(1, body.length);
            assertEquals("Harry Potter", body[0].getTitle());
        }
    }

    @Nested
    @DisplayName("GET /api/livros/{id}")
    class BuscarPorIdTests {

        @Test
        @DisplayName("Deve retornar livro existente pelo ID")
        void buscarPorId_existente_deveRetornar200() {
            ResponseEntity<Livro> criado = restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("1984", "George Orwell"), headersComJwt()), Livro.class);
            String id = Objects.requireNonNull(Objects.requireNonNull(criado.getBody()).getId());

            ResponseEntity<Livro> response = restTemplate.exchange(
                    livrosUrl + "/" + id, HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()), Livro.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("1984", Objects.requireNonNull(response.getBody()).getTitle());
        }

        @Test
        @DisplayName("Deve retornar 404 para ID inexistente")
        void buscarPorId_inexistente_deveRetornar404() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(livrosUrl + "/id-inexistente-xyz", HttpMethod.GET,
                            new HttpEntity<>(headersComJwt()), Livro.class)
            );
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("PUT /api/livros/{id}")
    class AtualizarLivroTests {

        @Test
        @DisplayName("Deve atualizar livro e retornar 200 com dados atualizados")
        void atualizar_comDadosValidos_deveRetornar200() {
            ResponseEntity<Livro> criado = restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Titulo Original", "Autor Original"), headersComJwt()), Livro.class);
            String id = Objects.requireNonNull(Objects.requireNonNull(criado.getBody()).getId());

            Livro atualizado = novoLivro("Titulo Atualizado", "Autor Atualizado");

            ResponseEntity<Livro> response = restTemplate.exchange(
                    livrosUrl + "/" + id, HttpMethod.PUT,
                    new HttpEntity<>(atualizado, headersComJwt()), Livro.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Titulo Atualizado", Objects.requireNonNull(response.getBody()).getTitle());
            assertEquals("Autor Atualizado", response.getBody().getAuthor());
        }
    }

    @Nested
    @DisplayName("DELETE /api/livros/{id}")
    class RemoverLivroTests {

        @Test
        @DisplayName("Deve remover livro e retornar 204")
        void remover_existente_deveRetornar204() {
            ResponseEntity<Livro> criado = restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Livro Para Deletar", "Autor"), headersComJwt()), Livro.class);
            String id = Objects.requireNonNull(Objects.requireNonNull(criado.getBody()).getId());

            ResponseEntity<Void> response = restTemplate.exchange(
                    livrosUrl + "/" + id, HttpMethod.DELETE,
                    new HttpEntity<>(headersComJwt()), Void.class);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 404 ao tentar remover livro inexistente")
        void remover_inexistente_deveRetornar404() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(livrosUrl + "/id-inexistente-xyz", HttpMethod.DELETE,
                            new HttpEntity<>(headersComJwt()), Void.class)
            );
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("GET /api/livros/nao-tenho")
    class NaoTenhoTests {

        @Test
        @DisplayName("Deve retornar livros da comunidade que o usuario nao possui")
        void naoTenho_deveRetornarLivrosDeComunidade() {
            String token2 = registrarEObterToken("outro@naotenho.com", "senha456");
            HttpHeaders h2 = new HttpHeaders();
            h2.setContentType(MediaType.APPLICATION_JSON);
            h2.setBearerAuth(token2);
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Livro Exclusivo Outro", "Autor X"), h2), Livro.class);

            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl + "/nao-tenho", HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()), Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Livro[] body = Objects.requireNonNull(response.getBody());
            assertTrue(body.length > 0, "Deve retornar ao menos um livro da comunidade");
            assertTrue(java.util.Arrays.stream(body)
                    .anyMatch(l -> "Livro Exclusivo Outro".equals(l.getTitle())));
        }

        @Test
        @DisplayName("Nao deve retornar livro que o proprio usuario ja possui")
        void naoTenho_naoDeveRetornarLivroQueJaPossui() {
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Meu Proprio Livro", "Autor M"), headersComJwt()), Livro.class);

            String token2 = registrarEObterToken("outro2@naotenho.com", "senha789");
            HttpHeaders h2 = new HttpHeaders();
            h2.setContentType(MediaType.APPLICATION_JSON);
            h2.setBearerAuth(token2);
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Meu Proprio Livro", "Autor M"), h2), Livro.class);

            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl + "/nao-tenho", HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()), Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Livro[] body = Objects.requireNonNull(response.getBody());
            assertTrue(java.util.Arrays.stream(body)
                    .noneMatch(l -> "Meu Proprio Livro".equals(l.getTitle())),
                    "Livro que o usuario ja possui nao deve aparecer em nao-tenho");
        }
    }

    @Nested
    @DisplayName("GET /api/livros/descobrir")
    class DescobrirTests {

        @Test
        @DisplayName("Deve retornar livros de outros usuarios com capa")
        void descobrir_deveRetornarLivros() {
            String token2 = registrarEObterToken("descobrir@email.com", "senha789");
            HttpHeaders h2 = new HttpHeaders();
            h2.setContentType(MediaType.APPLICATION_JSON);
            h2.setBearerAuth(token2);
            restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Livro Para Descobrir", "Autor D"), h2), Livro.class);

            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl + "/descobrir", HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()), Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("DELETE /api/livros/{id}/permanente")
    class ExcluirPermanenteTests {

        @Test
        @DisplayName("Deve excluir permanentemente quando chamado pelo criador e retornar 204")
        void excluirPermanente_peloCriador_deveRetornar204() {
            ResponseEntity<Livro> criado = restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Livro Permanente", "Autor P"), headersComJwt()), Livro.class);
            String id = Objects.requireNonNull(Objects.requireNonNull(criado.getBody()).getId());

            ResponseEntity<Void> response = restTemplate.exchange(
                    livrosUrl + "/" + id + "/permanente", HttpMethod.DELETE,
                    new HttpEntity<>(headersComJwt()), Void.class);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 403 ao tentar excluir permanentemente livro de outro criador")
        void excluirPermanente_porNaoCriador_deveRetornar403() {
            ResponseEntity<Livro> criado = restTemplate.exchange(livrosUrl, HttpMethod.POST,
                    new HttpEntity<>(novoLivro("Livro Compartilhado", "Autor C"), headersComJwt()), Livro.class);
            String id = Objects.requireNonNull(Objects.requireNonNull(criado.getBody()).getId());

            String token2 = registrarEObterToken("outro@email.com", "senha456");
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            headers2.setBearerAuth(token2);

            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.exchange(livrosUrl + "/" + id + "/permanente", HttpMethod.DELETE,
                            new HttpEntity<>(headers2), Void.class)
            );
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    private String registrarEObterToken(String email, String senha) {
        RegisterRequest reg = new RegisterRequest();
        reg.setNome("Usuario Teste E2E");
        reg.setEmail(email);
        reg.setSenha(senha);
        reg.setCep("01310-100");
        reg.setLogradouro("Av. Paulista");
        reg.setBairro("Bela Vista");
        reg.setCidade("Sao Paulo");
        reg.setUf("SP");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/register", reg, AuthResponse.class);
        AuthResponse body = Objects.requireNonNull(response.getBody(), "AuthResponse não pode ser null");
        return Objects.requireNonNull(body.getToken(), "JWT token não pode ser null");
    }

    private HttpHeaders headersComJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        return headers;
    }

    private static Livro novoLivro(String titulo, String autor) {
        Livro livro = new Livro();
        livro.setTitle(titulo);
        livro.setAuthor(autor);
        livro.setStatus("QUERO LER");
        livro.setPages(200);
        livro.setCover("https://example.com/capa.jpg");
        livro.setExcerpt("Sinopse de " + titulo);
        livro.setLanguage("Portugues");
        livro.setPublisher("Editora Exemplo");
        livro.setPublishedDate("2024-01-01");
        return livro;
    }
}
