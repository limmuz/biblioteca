package com.qs.biblioteca.e2e;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
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

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E – UsuarioController (/me, /leitores, /pesquisar, /perfil)")
class UsuarioE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String baseUrl;
    private String jwtToken;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api";
        usuarioRepository.deleteAll();

        RegisterRequest req = new RegisterRequest();
        req.setNome("Ana Teste");
        req.setEmail("ana@teste.com");
        req.setSenha("senha123");

        ResponseEntity<AuthResponse> res = restTemplate.postForEntity(
                baseUrl + "/auth/register", req, AuthResponse.class);

        jwtToken = Objects.requireNonNull(res.getBody()).getToken();
    }

    private HttpEntity<Void> comToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<String> comTokenEJson(String payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    @Nested
    @DisplayName("GET /api/usuarios/me")
    class MeTests {

        @Test
        @DisplayName("Deve retornar perfil do usuario autenticado")
        void me_autenticado_deveRetornarPerfil() {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.GET,
                    comToken(),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = Objects.requireNonNull(response.getBody());
            assertEquals("ana@teste.com", body.get("email"));
            assertEquals("Ana Teste", body.get("nome"));
        }

        @Test
        @DisplayName("Deve retornar 401 sem token de autenticacao")
        void me_semToken_deveRetornar401() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () ->
                    restTemplate.getForEntity(baseUrl + "/usuarios/me", String.class)
            );
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("PUT /api/usuarios/me")
    class AtualizarTests {

        @Test
        @DisplayName("Deve atualizar nome do usuario e retornar dados atualizados")
        void atualizar_comNomeNovo_deveRetornarUsuarioAtualizado() {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.PUT,
                    comTokenEJson("{\"nome\": \"Ana Atualizada\", \"bio\": \"Leitora\"}"),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = Objects.requireNonNull(response.getBody());
            assertEquals("Ana Atualizada", body.get("nome"));
        }

        @Test
        @DisplayName("Deve atualizar nickname, metaLeitura e perfilPublico")
        void atualizar_camposExtras_deveAtualizarTodos() {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.PUT,
                    comTokenEJson("{\"nickname\": \"nickextra\", \"bio\": \"Leitora apaixonada\", \"metaLeitura\": 24, \"perfilPublico\": true}"),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<String, Object> body = Objects.requireNonNull(response.getBody());
            assertEquals("nickextra", body.get("nickname"));
        }

        @Test
        @DisplayName("Deve atualizar listas de telefones e redesSociais")
        void atualizar_listas_deveAtualizar() {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.PUT,
                    comTokenEJson("{\"telefones\": [\"11999999999\"], \"redesSociais\": [\"@ana\"]}"),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Deve retornar 401 ao tentar atualizar sem token")
        void atualizar_semToken_deveRetornar401() {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = baseUrl + "/usuarios/me";
            HttpEntity<String> entity = new HttpEntity<>("{\"nome\": \"Teste\"}", headers);
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> restTemplate.exchange(url, HttpMethod.PUT, entity, String.class));
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("DELETE /api/usuarios/me")
    class ExcluirTests {

        @Test
        @DisplayName("Deve excluir conta do usuario e retornar 204")
        void excluir_deveRetornar204() {
            ResponseEntity<Void> response = restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.DELETE,
                    comToken(),
                    Void.class
            );

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("GET /api/usuarios/leitores")
    class LeitoresTests {

        @Test
        @DisplayName("Deve listar leitores publicos sem incluir o proprio usuario")
        void leitores_deveRetornarLista() {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    baseUrl + "/usuarios/leitores",
                    HttpMethod.GET,
                    comToken(),
                    Object[].class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("GET /api/usuarios/pesquisar")
    class PesquisarTests {

        @Test
        @DisplayName("Deve pesquisar por nome e retornar lista")
        void pesquisar_porNome_deveRetornarLista() {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    baseUrl + "/usuarios/pesquisar?q=Ana",
                    HttpMethod.GET,
                    comToken(),
                    Object[].class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Deve pesquisar por nickname com @ e retornar lista")
        void pesquisar_porNickname_deveRetornarLista() {
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    baseUrl + "/usuarios/pesquisar?q=@algumnick",
                    HttpMethod.GET,
                    comToken(),
                    Object[].class
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("GET /api/usuarios/perfil/{nickname} e /perfil/id/{id}")
    class PerfilPublicoTests {

        @Test
        @DisplayName("Deve retornar perfil publico por nickname")
        void perfilPorNickname_deveRetornarPerfil() {
            restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.PUT,
                    comTokenEJson("{\"nickname\": \"anateste\", \"perfilPublico\": true}"),
                    Void.class
            );

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/usuarios/perfil/anateste",
                    HttpMethod.GET,
                    comToken(),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Deve retornar 403 para perfil privado acessado por nickname")
        void perfilPorNickname_perfilPrivado_deveRetornar403() {
            restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.PUT,
                    comTokenEJson("{\"nickname\": \"nickprivado\", \"perfilPublico\": false}"),
                    Void.class
            );

            String url = baseUrl + "/usuarios/perfil/nickprivado";
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> restTemplate.exchange(url, HttpMethod.GET, comToken(), String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar perfil publico pelo ID do usuario")
        void perfilPorId_deveRetornarPerfil() {
            ResponseEntity<Map<String, Object>> meRes = restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.GET,
                    comToken(),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            String id = (String) Objects.requireNonNull(meRes.getBody()).get("id");

            restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.PUT,
                    comTokenEJson("{\"perfilPublico\": true}"),
                    Void.class
            );

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/usuarios/perfil/id/" + id,
                    HttpMethod.GET,
                    comToken(),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }

        @Test
        @DisplayName("Deve retornar 403 para perfil privado acessado pelo ID")
        void perfilPorId_perfilPrivado_deveRetornar403() {
            ResponseEntity<Map<String, Object>> meRes = restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.GET,
                    comToken(),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            String id = (String) Objects.requireNonNull(meRes.getBody()).get("id");

            String url = baseUrl + "/usuarios/perfil/id/" + id;
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> restTemplate.exchange(url, HttpMethod.GET, comToken(), String.class));
            assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("GET /api/usuarios/buscar")
    class BuscarPorNicknameTests {

        @Test
        @DisplayName("Deve buscar usuario publico por nickname exato")
        void buscar_porNickname_deveRetornarPerfil() {
            restTemplate.exchange(
                    baseUrl + "/usuarios/me",
                    HttpMethod.PUT,
                    comTokenEJson("{\"nickname\": \"nickbusca\", \"perfilPublico\": true}"),
                    Void.class
            );

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/usuarios/buscar?nickname=nickbusca",
                    HttpMethod.GET,
                    comToken(),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }
    }
}
