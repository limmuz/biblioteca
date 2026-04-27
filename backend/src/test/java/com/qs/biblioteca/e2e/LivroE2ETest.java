package com.qs.biblioteca.e2e;

import com.qs.biblioteca.TestcontainersConfiguration;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate; // <-- Import correto
import org.springframework.web.client.HttpClientErrorException; // <-- Necessario para pegar os 404/401

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("E2E – LivroController (CRUD completo com JWT)")
class LivroE2ETest {

    @LocalServerPort
    private int port;

    // Substituído o TestRestTemplate pelo RestTemplate nativo e removido o @Autowired
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
        baseUrl  = "http://localhost:" + port;
        livrosUrl = baseUrl + "/api/livros";

        livroRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Registra um usuário e obtém o JWT para autenticar as requisições
        jwtToken = registrarEObterToken("teste@email.com", "senha123");
    }

    // ══════════════════════════════════════════════════════════════
    // POST /api/livros
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/livros")
    class CriarLivroTests {

        @Test
        @DisplayName("Deve criar livro e retornar 200 com o livro salvo")
        void criar_comDadosValidos_deveRetornar200() {
            Livro novoLivro = novoLivro("Dom Casmurro", "Machado de Assis");

            ResponseEntity<Livro> response = restTemplate.exchange(
                    livrosUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(novoLivro, headersComJwt()),
                    Livro.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getId(), "ID deve ser gerado automaticamente");
            assertEquals("Dom Casmurro",      response.getBody().getTitle());
            assertEquals("Machado de Assis",  response.getBody().getAuthor());
            assertEquals("QUERO LER",         response.getBody().getStatus());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request ao criar livro sem titulo")
        void criar_semTitulo_deveRetornar400() {
            Livro livroSemTitulo = new Livro();
            livroSemTitulo.setAuthor("Autor Qualquer");
            livroSemTitulo.setStatus("QUERO LER");

            try {
                restTemplate.exchange(livrosUrl, HttpMethod.POST, new HttpEntity<>(livroSemTitulo, headersComJwt()), String.class);
                fail("Deveria retornar 400");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized ao criar livro sem JWT")
        void criar_semJwt_deveRetornar401() {
            Livro livro = novoLivro("Livro Sem Auth", "Autor");

            try {
                restTemplate.postForEntity(livrosUrl, livro, String.class);
                fail("Deveria retornar 401");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve persistir o livro no banco apos criacao")
        void criar_devePersistirNoBanco() {
            Livro novoLivro = novoLivro("1984", "George Orwell");

            restTemplate.exchange(
                    livrosUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(novoLivro, headersComJwt()),
                    Livro.class);

            assertEquals(1, livroRepository.count(),
                    "Deve existir 1 livro no banco apos criacao");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // GET /api/livros
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/livros")
    class ListarLivrosTests {

        @Test
        @DisplayName("Deve listar todos os livros e retornar 200")
        void listar_deveRetornarTodosOsLivros() {
            salvarLivroNoBanco("Duna",      "Frank Herbert");
            salvarLivroNoBanco("Fundacao",  "Isaac Asimov");

            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().length);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nao ha livros")
        void listar_semLivros_deveRetornarListaVazia() {
            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().length);
        }

        @Test
        @DisplayName("Deve filtrar livros por titulo via parametro search")
        void listar_comSearch_deveFiltrarPorTitulo() {
            salvarLivroNoBanco("O Alquimista", "Paulo Coelho");
            salvarLivroNoBanco("Dom Casmurro", "Machado de Assis");

            ResponseEntity<Livro[]> response = restTemplate.exchange(
                    livrosUrl + "?search=alquimista",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Livro[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().length);
            assertEquals("O Alquimista", response.getBody()[0].getTitle());
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized ao listar sem JWT")
        void listar_semJwt_deveRetornar401() {
            try {
                restTemplate.getForEntity(livrosUrl, String.class);
                fail("Deveria retornar 401");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // GET /api/livros/{id}
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/livros/{id}")
    class BuscarLivroPorIdTests {

        @Test
        @DisplayName("Deve retornar 200 e o livro para ID existente")
        void buscarPorId_existente_deveRetornar200() {
            Livro salvo = salvarLivroNoBanco("Sapiens", "Yuval Noah Harari");

            ResponseEntity<Livro> response = restTemplate.exchange(
                    livrosUrl + "/" + salvo.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Livro.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Sapiens",            response.getBody().getTitle());
            assertEquals("Yuval Noah Harari",  response.getBody().getAuthor());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found para ID inexistente")
        void buscarPorId_inexistente_deveRetornar404() {
            try {
                restTemplate.exchange(livrosUrl + "/id-que-nao-existe", HttpMethod.GET, new HttpEntity<>(headersComJwt()), String.class);
                fail("Deveria retornar 404");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // PUT /api/livros/{id}
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/livros/{id}")
    class AtualizarLivroTests {

        @Test
        @DisplayName("Deve atualizar livro existente e retornar 200 com dados atualizados")
        void atualizar_existente_deveRetornar200() {
            Livro salvo = salvarLivroNoBanco("Titulo Original", "Autor Original");

            Livro livroAtualizado = new Livro();
            livroAtualizado.setTitle("Titulo Atualizado");
            livroAtualizado.setAuthor("Autor Atualizado");
            livroAtualizado.setStatus("LIDO");
            livroAtualizado.setPages(350);
            livroAtualizado.setCover("https://example.com/capa-nova.jpg");
            livroAtualizado.setExcerpt("Nova sinopse");
            livroAtualizado.setLanguage("Portugues");
            livroAtualizado.setPublisher("Editora Nova");
            livroAtualizado.setPublishedDate("2024-01-01");

            ResponseEntity<Livro> response = restTemplate.exchange(
                    livrosUrl + "/" + salvo.getId(),
                    HttpMethod.PUT,
                    new HttpEntity<>(livroAtualizado, headersComJwt()),
                    Livro.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Titulo Atualizado", response.getBody().getTitle());
            assertEquals("Autor Atualizado",  response.getBody().getAuthor());
            assertEquals("LIDO",              response.getBody().getStatus());
            assertEquals(350,                 response.getBody().getPages());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found ao atualizar ID inexistente")
        void atualizar_inexistente_deveRetornar404() {
            Livro livroAtualizado = novoLivro("Qualquer", "Qualquer");

            try {
                restTemplate.exchange(livrosUrl + "/id-inexistente", HttpMethod.PUT, new HttpEntity<>(livroAtualizado, headersComJwt()), String.class);
                fail("Deveria retornar 404");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Atualizacao deve refletir em consulta subsequente por ID")
        void atualizar_deveRefletirNaConsultaPosterior() {
            Livro salvo = salvarLivroNoBanco("Antes", "Autor");

            Livro payload = novoLivro("Depois", "Autor Novo");

            restTemplate.exchange(
                    livrosUrl + "/" + salvo.getId(),
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headersComJwt()),
                    Livro.class);

            ResponseEntity<Livro> consulta = restTemplate.exchange(
                    livrosUrl + "/" + salvo.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Livro.class);

            assertEquals(HttpStatus.OK, consulta.getStatusCode());
            assertNotNull(consulta.getBody());
            assertEquals("Depois",    consulta.getBody().getTitle());
            assertEquals("Autor Novo", consulta.getBody().getAuthor());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE /api/livros/{id}
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/livros/{id}")
    class DeletarLivroTests {

        @Test
        @DisplayName("Deve deletar livro existente e retornar 204 No Content")
        void deletar_existente_deveRetornar204() {
            Livro salvo = salvarLivroNoBanco("Livro a Deletar", "Autor Teste");

            // No Java RestTemplate puro, um DELETE 204 também pode devolver 200, então validamos que não lançou erro
            ResponseEntity<Void> response = restTemplate.exchange(
                    livrosUrl + "/" + salvo.getId(),
                    HttpMethod.DELETE,
                    new HttpEntity<>(headersComJwt()),
                    Void.class);
            
            assertTrue(response.getStatusCode().is2xxSuccessful());
        }

        @Test
        @DisplayName("Deve retornar 404 Not Found ao deletar ID inexistente")
        void deletar_inexistente_deveRetornar404() {
            try {
                restTemplate.exchange(livrosUrl + "/id-inexistente", HttpMethod.DELETE, new HttpEntity<>(headersComJwt()), String.class);
                fail("Deveria retornar 404");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Apos deletar, livro nao deve mais ser encontrado")
        void deletar_livroNaoDeveSerEncontradoDepois() {
            Livro salvo = salvarLivroNoBanco("Efemero", "Autor");
            String id = salvo.getId();

            restTemplate.exchange(
                    livrosUrl + "/" + id,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headersComJwt()),
                    Void.class);

            try {
                restTemplate.exchange(livrosUrl + "/" + id, HttpMethod.GET, new HttpEntity<>(headersComJwt()), String.class);
                fail("O livro deveria ter sido deletado (404)");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            }

            assertFalse(livroRepository.existsById(id), "Livro nao deve existir no banco apos delecao");
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized ao deletar sem JWT")
        void deletar_semJwt_deveRetornar401() {
            Livro salvo = salvarLivroNoBanco("Qualquer", "Autor");

            try {
                restTemplate.exchange(livrosUrl + "/" + salvo.getId(), HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
                fail("Deveria retornar 401");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    // GET /api/usuarios/me (cobre UsuarioController)
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/usuarios/me")
    class UsuarioMeTests {

        @Test
        @DisplayName("Deve retornar dados do usuario autenticado com JWT valido")
        void me_comJwtValido_deveRetornar200() {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    String.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().contains("teste@email.com"),
                    "Resposta deve conter o email do usuario autenticado");
        }

        @Test
        @DisplayName("Deve retornar 401 ao acessar /me sem JWT")
        void me_semJwt_deveRetornar401() {
            try {
                restTemplate.exchange(baseUrl + "/api/usuarios/me", HttpMethod.GET, HttpEntity.EMPTY, String.class);
                fail("Deveria retornar 401");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            }
        }
    }

    // ── helpers privados ──────────────────────────────────────────

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

        assertNotNull(response.getBody(), "Registro deve retornar body com token");
        return response.getBody().getToken();
    }

    private HttpHeaders headersComJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        return headers;
    }

    private Livro salvarLivroNoBanco(String titulo, String autor) {
        Livro l = novoLivro(titulo, autor);
        return livroRepository.save(l);
    }

    private static Livro novoLivro(String titulo, String autor) {
        Livro l = new Livro();
        l.setTitle(titulo);
        l.setAuthor(autor);
        l.setStatus("QUERO LER");
        l.setPages(200);
        l.setCover("https://example.com/capa.jpg");
        l.setExcerpt("Sinopse de " + titulo);
        l.setLanguage("Portugues");
        l.setPublisher("Editora Exemplo");
        l.setPublishedDate("2024-01-01");
        return l;
    }
}