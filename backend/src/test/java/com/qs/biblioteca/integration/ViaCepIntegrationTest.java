package com.qs.biblioteca.integration;

import com.qs.biblioteca.BaseMongoTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Integração – CepController com API ViaCEP real")
class ViaCepIntegrationTest extends BaseMongoTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    @DisplayName("deve retornar endereço para CEP válido (01310-100 – Av. Paulista)")
    void buscarCep_valido_deveRetornarEndereco() {
        String url = "http://localhost:" + port + "/api/cep/01310-100";

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Resposta não deve ser nula");
        assertTrue(body.containsKey("logradouro"), "Resposta deve conter o campo logradouro");
        assertEquals("SP", body.get("uf"));
    }

    @Test
    @DisplayName("deve retornar 404 para CEP inexistente")
    void buscarCep_invalido_deveRetornar404() {
        String url = "http://localhost:" + port + "/api/cep/00000-000";

        HttpClientErrorException ex = assertThrows(
                HttpClientErrorException.class,
                () -> restTemplate.getForEntity(url, Map.class));

        assertEquals(404, ex.getStatusCode().value());
    }
}
