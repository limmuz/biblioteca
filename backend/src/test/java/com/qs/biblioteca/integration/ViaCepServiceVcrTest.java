package com.qs.biblioteca.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.qs.biblioteca.service.ViaCepService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
@DisplayName("VCR – ViaCepService com cassete gravado da API real (sem rede)")
class ViaCepServiceVcrTest {

    private String carregarCassete(String arquivo) {
        try (InputStream is = getClass().getResourceAsStream("/vcr/" + arquivo)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cassete não encontrado: " + arquivo, e);
        }
    }

    @Test
    @DisplayName("CEP válido 01310-100 — retorna logradouro e UF da resposta gravada")
    void buscarCep_valido_deveRetornarLogradouroEUf(WireMockRuntimeInfo wm) {
        stubFor(get("/01310100/json").willReturn(okJson(carregarCassete("viacep_01310-100.json"))));

        ViaCepService service = new ViaCepService("http://localhost:" + wm.getHttpPort());
        Map<String, Object> resultado = service.buscarEnderecoPorCep("01310-100");

        assertEquals("SP", resultado.get("uf"));
        assertEquals("Avenida Paulista", resultado.get("logradouro"));
        assertEquals("Bela Vista", resultado.get("bairro"));
    }

    @Test
    @DisplayName("CEP inexistente 00000-000 — API retorna campo 'erro'")
    void buscarCep_inexistente_deveConterCampoErro(WireMockRuntimeInfo wm) {
        stubFor(get("/00000000/json").willReturn(okJson(carregarCassete("viacep_00000-000.json"))));

        ViaCepService service = new ViaCepService("http://localhost:" + wm.getHttpPort());
        Map<String, Object> resultado = service.buscarEnderecoPorCep("00000-000");

        assertTrue(resultado.containsKey("erro"));
    }

    @Test
    @DisplayName("CEP nulo — retorna mapa vazio sem consultar a API")
    void buscarCep_nulo_deveRetornarMapaVazio(WireMockRuntimeInfo wm) {
        ViaCepService service = new ViaCepService("http://localhost:" + wm.getHttpPort());
        assertTrue(service.buscarEnderecoPorCep(null).isEmpty());
        WireMock.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("CEP com dígitos insuficientes — retorna mapa vazio sem consultar a API")
    void buscarCep_digitsInsuficientes_deveRetornarMapaVazio(WireMockRuntimeInfo wm) {
        ViaCepService service = new ViaCepService("http://localhost:" + wm.getHttpPort());
        assertTrue(service.buscarEnderecoPorCep("1234-56").isEmpty());
        WireMock.verify(0, anyRequestedFor(anyUrl()));
    }
}
