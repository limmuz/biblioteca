package com.qs.biblioteca.e2e;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.dto.AuthRequest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E – Fluxo de Autenticacao (Auth API)")
class AuthE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api/auth";
        usuarioRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegistroTests {

        @Test
        @DisplayName("Deve registrar usuario e retornar 200 com token JWT")
        void register_comDadosValidos_deveRetornarToken() {
            RegisterRequest request = novoRegisterRequest(
                    "Joao Silva", "joao@email.com", "senha123");

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    baseUrl + "/register", request, AuthResponse.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getToken(), "Token nao deve ser nulo");
            assertFalse(response.getBody().getToken().isBlank(), "Token nao deve ser vazio");
            assertEquals("Joao Silva",    response.getBody().getNome());
            assertEquals("joao@email.com", response.getBody().getEmail());
        }

        @Test
        @DisplayName("Deve retornar 409 Conflict ao tentar registrar email ja existente")
        void register_comEmailDuplicado_deveRetornar409() {
            RegisterRequest request = novoRegisterRequest(
                    "Maria Souza", "maria@email.com", "senha123");

            restTemplate.postForEntity(baseUrl + "/register", request, AuthResponse.class);

            try {
                restTemplate.postForEntity(baseUrl + "/register", request, String.class);
                fail("Deveria ter jogado uma exceção HTTP 409");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para email invalido")
        void register_comEmailInvalido_deveRetornar400() {
            RegisterRequest request = novoRegisterRequest(
                    "Usuario Teste", "email-invalido-sem-arroba", "senha123");

            try {
                restTemplate.postForEntity(baseUrl + "/register", request, String.class);
                fail("Deveria ter jogado uma exceção HTTP 400");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para senha muito curta")
        void register_comSenhaCurta_deveRetornar400() {
            RegisterRequest request = novoRegisterRequest(
                    "Usuario Teste", "usuario@email.com", "12345");

            try {
                restTemplate.postForEntity(baseUrl + "/register", request, String.class);
                fail("Deveria ter jogado uma exceção HTTP 400");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve salvar o usuario no banco apos registro bem-sucedido")
        void register_devePersistirUsuarioNoBanco() {
            RegisterRequest request = novoRegisterRequest(
                    "Carlos Lima", "carlos@email.com", "senha456");

            restTemplate.postForEntity(baseUrl + "/register", request, AuthResponse.class);

            assertTrue(usuarioRepository.existsByEmail("carlos@email.com"),
                    "Usuario deve existir no banco apos registro");
        }
    }

    @Nested
    @DisplayName("POST /api/auth/redefinir-senha")
    class RedefinirSenhaTests {

        @BeforeEach
        void registrarUsuario() {
            RegisterRequest reg = novoRegisterRequest(
                    "Teste Redefinir", "redefinir@email.com", "senha123");
            restTemplate.postForEntity(baseUrl + "/register", reg, AuthResponse.class);
        }

        @Test
        @DisplayName("Deve redefinir senha com credenciais corretas e retornar 204")
        void redefinirSenha_comCredenciaisCorretas_deveRetornar204() {
            var body = java.util.Map.of(
                    "email", "redefinir@email.com",
                    "senhaAtual", "senha123",
                    "novaSenha", "novaSenha1");

            ResponseEntity<Void> response = restTemplate.postForEntity(
                    baseUrl + "/redefinir-senha", body, Void.class);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 401 quando senha atual estiver incorreta")
        void redefinirSenha_comSenhaAtualErrada_deveRetornar401() {
            var body = java.util.Map.of(
                    "email", "redefinir@email.com",
                    "senhaAtual", "senhaErrada1",
                    "novaSenha", "novaSenha1");

            try {
                restTemplate.postForEntity(baseUrl + "/redefinir-senha", body, String.class);
                fail("Deveria ter lancado excecao HTTP 401");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve retornar 404 quando email nao existir")
        void redefinirSenha_comEmailInexistente_deveRetornar404() {
            var body = java.util.Map.of(
                    "email", "naoexiste@email.com",
                    "senhaAtual", "senha123",
                    "novaSenha", "novaSenha1");

            try {
                restTemplate.postForEntity(baseUrl + "/redefinir-senha", body, String.class);
                fail("Deveria ter lancado excecao HTTP 404");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve retornar 400 quando campos obrigatorios estiverem ausentes")
        void redefinirSenha_semCamposObrigatorios_deveRetornar400() {
            var body = java.util.Map.of("email", "redefinir@email.com");

            try {
                restTemplate.postForEntity(baseUrl + "/redefinir-senha", body, String.class);
                fail("Deveria ter lancado excecao HTTP 400");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @BeforeEach
        void registrarUsuarioPadrao() {
            RegisterRequest reg = novoRegisterRequest(
                    "Ana Pereira", "ana@email.com", "minhasenha1");
            restTemplate.postForEntity(baseUrl + "/register", reg, AuthResponse.class);
        }

        @Test
        @DisplayName("Deve fazer login com credenciais corretas e retornar JWT valido")
        void login_comCredenciaisCorretas_deveRetornarJwt() {
            AuthRequest request = new AuthRequest();
            request.setEmail("ana@email.com");
            request.setSenha("minhasenha1");

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    baseUrl + "/login", request, AuthResponse.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getToken(), "JWT nao deve ser nulo");
            assertFalse(response.getBody().getToken().isBlank());
            assertEquals("Ana Pereira",  response.getBody().getNome());
            assertEquals("ana@email.com", response.getBody().getEmail());
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized com senha incorreta")
        void login_comSenhaErrada_deveRetornar401() {
            AuthRequest request = new AuthRequest();
            request.setEmail("ana@email.com");
            request.setSenha("senhaerrada");

            try {
                restTemplate.postForEntity(baseUrl + "/login", request, String.class);
                fail("Deveria ter jogado uma exceção HTTP 401");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Deve retornar 401 Unauthorized com email nao cadastrado")
        void login_comEmailNaoCadastrado_deveRetornar401() {
            AuthRequest request = new AuthRequest();
            request.setEmail("naoexiste@email.com");
            request.setSenha("qualquersenha");

            try {
                restTemplate.postForEntity(baseUrl + "/login", request, String.class);
                fail("Deveria ter jogado uma exceção HTTP 401");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
            }
        }

        @Test
        @DisplayName("Login deve ser case-insensitive para o email")
        void login_comEmailEmMaiusculo_deveAutenticar() {
            AuthRequest request = new AuthRequest();
            request.setEmail("ANA@EMAIL.COM");
            request.setSenha("minhasenha1");

            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    baseUrl + "/login", request, AuthResponse.class);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().getToken());
        }

        @Test
        @DisplayName("Deve retornar 400 Bad Request para email mal formatado no login")
        void login_comEmailMalFormatado_deveRetornar400() {
            AuthRequest request = new AuthRequest();
            request.setEmail("nao-e-um-email");
            request.setSenha("senha123");

            try {
                restTemplate.postForEntity(baseUrl + "/login", request, String.class);
                fail("Deveria ter jogado uma exceção HTTP 400");
            } catch (HttpClientErrorException e) {
                assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            }
        }
    }

    private static RegisterRequest novoRegisterRequest(
            String nome, String email, String senha) {
        RegisterRequest req = new RegisterRequest();
        req.setNome(nome);
        req.setEmail(email);
        req.setSenha(senha);
        req.setCep("01310-100");
        req.setLogradouro("Av. Paulista");
        req.setBairro("Bela Vista");
        req.setCidade("Sao Paulo");
        req.setUf("SP");
        return req;
    }
}