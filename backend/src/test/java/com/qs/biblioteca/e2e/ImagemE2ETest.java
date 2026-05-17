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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("E2E – ImagemController (POST /api/imagens/upload)")
class ImagemE2ETest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String uploadUrl;
    private String jwtToken;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
        uploadUrl = "http://localhost:" + port + "/api/imagens/upload";

        RegisterRequest reg = new RegisterRequest();
        reg.setNome("Usuario Imagem");
        reg.setEmail("imagem@teste.com");
        reg.setSenha("senha123");

        ResponseEntity<AuthResponse> res = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/auth/register", reg, AuthResponse.class);
        jwtToken = Objects.requireNonNull(Objects.requireNonNull(res.getBody()).getToken());
    }

    @Nested
    @DisplayName("POST /api/imagens/upload")
    class UploadTests {

        @Test
        @DisplayName("Deve retornar 401 ao tentar fazer upload sem token")
        void upload_semToken_deveRetornar401() {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", arquivoFake());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class));
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        }

        @Test
        @DisplayName("Deve retornar 503 quando Cloudinary nao esta configurado no perfil de teste")
        void upload_semCloudinaryConfigurado_deveRetornar503() {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", arquivoFake());
            body.add("pasta", "avatares");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            HttpServerErrorException ex = assertThrows(HttpServerErrorException.class,
                    () -> restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class));
            assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        }
    }

    private static ByteArrayResource arquivoFake() {
        return new ByteArrayResource(new byte[]{-1, -40, -1, -32}) {
            @Override
            public String getFilename() { return "test.jpg"; }
        };
    }
}
