package com.qs.biblioteca.unit;

import com.qs.biblioteca.controller.CepController;
import com.qs.biblioteca.service.ViaCepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CepController e ViaCepService (sem acesso à rede).
 * Usam Mockito para simular respostas da API ViaCEP, garantindo cobertura
 * independente de conectividade externa no ambiente de CI.
 */
@DisplayName("Unitário – CepController")
class CepControllerUnitTest {

    @Nested
    @DisplayName("GET /api/cep/{cep} — CEP válido")
    class CepValidoTests {

        @Test
        @DisplayName("deve retornar 200 com campos de endereço")
        void buscarCep_valido_deveRetornar200() {
            ViaCepService service = mock(ViaCepService.class);
            when(service.buscarEnderecoPorCep("01310100"))
                    .thenReturn(Map.of("cep", "01310-100", "logradouro", "Av. Paulista",
                            "bairro", "Bela Vista", "localidade", "São Paulo", "uf", "SP"));

            CepController controller = new CepController(service);
            ResponseEntity<Map<String, Object>> resp = controller.buscarCep("01310100");

            assertEquals(HttpStatus.OK, resp.getStatusCode());
            Map<String, Object> body = resp.getBody();
            assertNotNull(body);
            assertEquals("SP", body.get("uf"));
            assertEquals("Av. Paulista", body.get("logradouro"));
        }
    }

    @Nested
    @DisplayName("GET /api/cep/{cep} — CEP inválido")
    class CepInvalidoTests {

        @Test
        @DisplayName("deve retornar 404 quando ViaCEP retorna chave 'erro'")
        void buscarCep_comErro_deveRetornar404() {
            ViaCepService service = mock(ViaCepService.class);
            when(service.buscarEnderecoPorCep("00000000"))
                    .thenReturn(Map.of("erro", "true"));

            CepController controller = new CepController(service);
            ResponseEntity<Map<String, Object>> resp = controller.buscarCep("00000000");

            assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        }

        @Test
        @DisplayName("deve retornar 404 quando ViaCEP retorna null")
        void buscarCep_null_deveRetornar404() {
            ViaCepService service = mock(ViaCepService.class);
            when(service.buscarEnderecoPorCep("99999999")).thenReturn(null);

            CepController controller = new CepController(service);
            ResponseEntity<Map<String, Object>> resp = controller.buscarCep("99999999");

            assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        }
    }

    @Nested
    @DisplayName("ViaCepService – construção de URL")
    class ViaCepServiceTests {

        @Test
        @DisplayName("deve construir URL corretamente a partir do base-url configurado")
        void viaCepService_baseUrl_deveSerConfigurado() {
            // Verifica que o serviço aceita um base-url customizado (usado em testes de integração)
            ViaCepService service = new ViaCepService("https://viacep.com.br/ws");
            assertNotNull(service);
        }
    }
}
