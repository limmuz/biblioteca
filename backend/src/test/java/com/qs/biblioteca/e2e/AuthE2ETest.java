package com.qs.biblioteca.e2e;

import com.qs.biblioteca.TestcontainersConfiguration;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate; // <-- Importação corrigida
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("E2E – Fluxo de Autenticacao (Auth API)")
class AuthE2ETest {

    @LocalServerPort
    private int port;

    // Substituído o TestRestTemplate problemático pelo RestTemplate real nativo
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api/auth";
        usuarioRepository.deleteAll();
    }

    // ══════════════════════════════════════════════════════════════
    // REGISTRO
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        @BeforeEach
        void registrarUsuarioPadrao() {
            RegisterRequest reg = novoRegisterRequest(
                    "Ana Pereira", "ana@email.com", "minhasenha");
            restTemplate.postForEntity(baseUrl + "/register", reg, AuthResponse.class);
        }

        @Test
        @DisplayName("Deve fazer login com credenciais corretas e retornar JWT valido")
        void login_comCredenciaisCorretas_deveRetornarJwt() {
            AuthRequest request = new AuthRequest();
            request.setEmail("ana@email.com");
            request.setSenha("minhasenha");

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
            request.setEmail("ANA@EMAIL.COM"); // maiúsculo
            request.setSenha("minhasenha");

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

    // ── factory auxiliar ──────────────────────────────────────────

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