package com.qs.biblioteca.e2e;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
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
@DisplayName("E2E – UsuarioController (perfil, personalização e perfil público)")
@SuppressWarnings({"rawtypes", "unchecked", "null"})
class UsuarioE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LivroRepository livroRepository;

    private String baseUrl;
    private String jwtToken;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
        usuarioRepository.deleteAll();
        livroRepository.deleteAll();
        jwtToken = registrarEObterToken("usuario@email.com", "senha123", "leitora_teste");
    }


    @Nested
    @DisplayName("GET /api/usuarios/me")
    class BuscarMeTests {

        @Test
        @DisplayName("Deve retornar dados do usuario autenticado com status 200")
        void me_autenticado_deveRetornar200ComDados() {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Leitor Teste", response.getBody().get("nome"));
            assertEquals("usuario@email.com", response.getBody().get("email"));
        }

        @Test
        @DisplayName("Deve retornar 403 sem JWT")
        void me_semJwt_deveRetornar403() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(baseUrl + "/api/usuarios/me", String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }


    @Nested
    @DisplayName("PUT /api/usuarios/me — personalização de perfil (RF-13)")
    class AtualizarPerfilTests {

        @Test
        @DisplayName("Deve salvar bio e retornar 200 com bio atualizada")
        void atualizar_bio_deveRetornar200() {
            var payload = Map.of("bio", "Apaixonada por livros de fantasia");

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Apaixonada por livros de fantasia", response.getBody().get("bio"));
        }

        @Test
        @DisplayName("Deve salvar metaLeitura e retornar 200")
        void atualizar_metaLeitura_deveRetornar200() {
            var payload = Map.of("metaLeitura", 24);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(24, response.getBody().get("metaLeitura"));
        }

        @Test
        @DisplayName("Deve salvar perfilPublico=false e persistir no banco")
        void atualizar_perfilPublico_deveRetornar200EPersistir() {
            var payload = Map.of("perfilPublico", false);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(false, response.getBody().get("perfilPublico"));

            var usuarioNoBanco = usuarioRepository.findByEmail("usuario@email.com");
            assertTrue(usuarioNoBanco.isPresent());
            assertFalse(usuarioNoBanco.get().isPerfilPublico());
        }

        @Test
        @DisplayName("Deve salvar bgBase64 e retornar no campo bgBase64")
        void atualizar_bgBase64_deveRetornar200ComCampo() {
            var payload = Map.of("bgBase64", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==");

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==",
                    response.getBody().get("bgBase64"));
        }

        @Test
        @DisplayName("Deve salvar nickname e retornar 200")
        void atualizar_nickname_deveRetornar200() {
            var payload = Map.of("nickname", "nova_leitora");

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(payload, headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("nova_leitora", response.getBody().get("nickname"));
        }

        @Test
        @DisplayName("Deve retornar 403 sem JWT")
        void atualizar_semJwt_deveRetornar403() {
            var req = new HttpEntity<>(Map.of("bio", "teste"));
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> restTemplate.exchange(baseUrl + "/api/usuarios/me", HttpMethod.PUT, req, String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }


    @Nested
    @DisplayName("GET /api/usuarios/perfil/:nickname — perfil público (RF-14)")
    class PerfilPublicoTests {

        @Test
        @DisplayName("Deve retornar perfil completo com bgBase64 para usuario publico")
        void perfil_publico_deveRetornar200ComDados() {
            restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(Map.of("bgBase64", "data:image/png;base64,abc123"), headersComJwt()),
                    Map.class);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/perfil/leitora_teste",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Leitor Teste", response.getBody().get("nome"));
            assertEquals("leitora_teste", response.getBody().get("nickname"));
            assertEquals("data:image/png;base64,abc123", response.getBody().get("bgBase64"));
        }

        @Test
        @DisplayName("Deve retornar 403 quando perfil e privado")
        void perfil_privado_deveRetornar403() {
            restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(Map.of("perfilPublico", false), headersComJwt()),
                    Map.class);

            var req = new HttpEntity<>(headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> restTemplate.exchange(baseUrl + "/api/usuarios/perfil/leitora_teste",
                            HttpMethod.GET, req, String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 404 para nickname inexistente")
        void perfil_nicknameInexistente_deveRetornar404() {
            var req = new HttpEntity<>(headersComJwt());
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> restTemplate.exchange(baseUrl + "/api/usuarios/perfil/nickname-que-nao-existe",
                            HttpMethod.GET, req, String.class));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve incluir totalLidos, totalLendo e totalQueroLer no perfil")
        void perfil_deveIncluirEstatisticas() {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/perfil/leitora_teste",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Map.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().get("totalLidos"));
            assertNotNull(response.getBody().get("totalLendo"));
            assertNotNull(response.getBody().get("totalQueroLer"));
        }
    }


    @Nested
    @DisplayName("GET /api/usuarios/leitores")
    class ListarLeitoresTests {

        @Test
        @DisplayName("Deve retornar lista de outros leitores publicos")
        void leitores_deveRetornarLista() {
            registrarEObterToken("outro@email.com", "senha123", "outro_leitor");

            ResponseEntity<Map[]> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/leitores",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Map[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().length);
        }

        @Test
        @DisplayName("Deve retornar 403 sem JWT")
        void leitores_semJwt_deveRetornar403() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(baseUrl + "/api/usuarios/leitores", String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }


    @Nested
    @DisplayName("GET /api/usuarios/pesquisar")
    class PesquisarLeitoresTests {

        @Test
        @DisplayName("Deve encontrar usuario por nome")
        void pesquisar_porNome_deveRetornarResultado() {
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/pesquisar?q=Leitor Teste",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Map[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().length > 0);
        }

        @Test
        @DisplayName("Deve encontrar usuario por nickname com @")
        void pesquisar_porNicknameComArroba_deveRetornarResultado() {
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/pesquisar?q=@leitora_teste",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Map[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().length);
            assertEquals("leitora_teste", response.getBody()[0].get("nickname"));
        }

        @Test
        @DisplayName("Deve retornar lista vazia para termo sem resultado")
        void pesquisar_semResultado_deveRetornarListaVazia() {
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    baseUrl + "/api/usuarios/pesquisar?q=xyz_nao_existe_123",
                    HttpMethod.GET,
                    new HttpEntity<>(headersComJwt()),
                    Map[].class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(0, response.getBody().length);
        }
    }


    private String registrarEObterToken(String email, String senha, String nickname) {
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
        String token = response.getBody().getToken();

        if (nickname != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            restTemplate.exchange(
                    baseUrl + "/api/usuarios/me",
                    HttpMethod.PUT,
                    new HttpEntity<>(Map.of("nickname", nickname), headers),
                    Map.class);
        }

        return token;
    }

    private HttpHeaders headersComJwt() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        return headers;
    }
}
