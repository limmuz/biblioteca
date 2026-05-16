package com.qs.biblioteca.integration;

import com.qs.biblioteca.BaseMongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Integração – CepController com API ViaCEP real")
class ViaCepIntegrationTest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    @SuppressWarnings("java:S108")
    void assumirViaCepAcessivel() {
        boolean acessivel = false;
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://viacep.com.br").openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("HEAD");
            acessivel = conn.getResponseCode() < 500;
            conn.disconnect();
        } catch (Exception ignored) {
        }
        assumeTrue(acessivel, "ViaCEP API não está acessível neste ambiente — teste ignorado");
    }

    @Test
    @DisplayName("deve retornar endereço para CEP válido (01310-100 – Av. Paulista)")
    void buscarCep_valido_deveRetornarEndereco() {
        String url = "http://localhost:" + port + "/api/cep/01310-100";

        HttpMethod method = Objects.requireNonNull(HttpMethod.GET);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                method, 
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Resposta não deve ser nula");
        assertTrue(body.containsKey("logradouro"), "Resposta deve conter o campo logradouro");
        assertEquals("SP", body.get("uf"));
    }

    @ParameterizedTest(name = "[{index}] {0} deve retornar HTTP {1}")
    @CsvSource({
        "00000-000, 404",
        "01310100,  400",
        "ABCDE-123, 400"
    })
    @DisplayName("deve retornar erro para CEP inexistente ou com formato inválido")
    void buscarCep_invalido_deveRetornarErroCorrerto(String cep, int expectedStatus) {
        String url = "http://localhost:" + port + "/api/cep/" + cep;

        HttpClientErrorException ex = assertThrows(
                HttpClientErrorException.class,
                () -> restTemplate.getForEntity(url, Map.class));

        assertEquals(expectedStatus, ex.getStatusCode().value());
    }
}
