package com.qs.biblioteca.vcr;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.qs.biblioteca.service.ViaCepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ViaCepService — VCR com WireMock")
class ViaCepServiceWireMockTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private ViaCepService service;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + wireMock.getPort() + "/ws";
        service = new ViaCepService(baseUrl, new RestTemplate());
    }

    @Nested
    @DisplayName("CEP válido")
    class CepValidoTests {

        @Test
        @DisplayName("deve retornar endereço completo quando CEP existe")
        void buscarCep_valido_deveRetornarEndereco() {
            wireMock.stubFor(get(urlEqualTo("/ws/01310-100/json/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                      "cep": "01310-100",
                                      "logradouro": "Avenida Paulista",
                                      "bairro": "Bela Vista",
                                      "localidade": "São Paulo",
                                      "uf": "SP"
                                    }
                                    """)));

            Map<String, Object> resultado = service.buscarEnderecoPorCep("01310-100");

            assertThat(resultado).containsEntry("cep", "01310-100");
            assertThat(resultado).containsEntry("logradouro", "Avenida Paulista");
            assertThat(resultado).containsEntry("localidade", "São Paulo");
            assertThat(resultado).containsEntry("uf", "SP");

            wireMock.verify(getRequestedFor(urlEqualTo("/ws/01310-100/json/")));
        }

        @Test
        @DisplayName("deve montar a URL corretamente com o CEP informado")
        void buscarCep_deveUsarCepNaUrl() {
            wireMock.stubFor(get(urlEqualTo("/ws/20040-020/json/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                    {
                                      "cep": "20040-020",
                                      "logradouro": "Avenida Rio Branco",
                                      "localidade": "Rio de Janeiro",
                                      "uf": "RJ"
                                    }
                                    """)));

            Map<String, Object> resultado = service.buscarEnderecoPorCep("20040-020");

            assertThat(resultado).containsEntry("localidade", "Rio de Janeiro");
            wireMock.verify(1, getRequestedFor(urlEqualTo("/ws/20040-020/json/")));
        }
    }

    @Nested
    @DisplayName("CEP inválido")
    class CepInvalidoTests {

        @Test
        @DisplayName("deve retornar resposta com campo 'erro' quando CEP não existe")
        void buscarCep_invalido_deveRetornarRespostaComErro() {
            wireMock.stubFor(get(urlEqualTo("/ws/00000-000/json/"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"erro\": \"true\"}")));

            Map<String, Object> resultado = service.buscarEnderecoPorCep("00000-000");

            assertThat(resultado).containsKey("erro");
        }
    }

    @Nested
    @DisplayName("API indisponível")
    class ApiIndisponivelTests {

        @Test
        @DisplayName("deve lançar exceção quando a API retorna 503")
        void buscarCep_apiIndisponivel_deveLancarExcecao() {
            wireMock.stubFor(get(urlEqualTo("/ws/01310-100/json/"))
                    .willReturn(aResponse()
                            .withStatus(503)
                            .withBody("Service Unavailable")));

            assertThrows(
                    HttpServerErrorException.class,
                    () -> service.buscarEnderecoPorCep("01310-100")
            );
        }
    }
}
