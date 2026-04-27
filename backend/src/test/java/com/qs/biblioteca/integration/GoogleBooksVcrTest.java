package com.qs.biblioteca.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes VCR com WireMock para GoogleBooksClient.
 *
 * WireMock sobe um servidor HTTP real na porta dinâmica e intercepta
 * as chamadas do cliente antes de chegarem à internet — sem Mockito,
 * sem mocks de classes Java.
 *
 * Dependência no pom.xml:
 *   <dependency>
 *       <groupId>org.wiremock</groupId>
 *       <artifactId>wiremock-standalone</artifactId>
 *       <version>3.5.2</version>
 *       <scope>test</scope>
 *   </dependency>
 */
@DisplayName("VCR – GoogleBooksClient com WireMock")
class GoogleBooksVcrTest {

    private WireMockServer wireMockServer;
    private GoogleBooksClient client;

    @BeforeEach
    void iniciarWireMock() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();

        String wireMockUrl = "http://localhost:" + wireMockServer.port();
        client = new GoogleBooksClient(wireMockUrl);
    }

    @AfterEach
    void pararWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 200 OK – livro encontrado
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("200 OK – deve retornar BookInfo com todos os campos")
    void buscarPorTitulo_200Ok_deveRetornarBookInfo() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/v1/volumes"))
                .withQueryParam("q", equalTo("Dom Casmurro"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "totalItems": 1,
                                  "items": [{
                                    "id": "abc123",
                                    "volumeInfo": {
                                      "title": "Dom Casmurro",
                                      "authors": ["Machado de Assis"],
                                      "description": "Narrado por Bento Santiago...",
                                      "pageCount": 256,
                                      "publisher": "Atica",
                                      "publishedDate": "1899",
                                      "categories": ["Ficcao", "Classico"]
                                    }
                                  }]
                                }
                                """)));

        Optional<GoogleBooksClient.BookInfo> resultado =
                client.buscarPorTitulo("Dom Casmurro");

        assertTrue(resultado.isPresent());
        assertEquals("Dom Casmurro",     resultado.get().getTitle());
        assertEquals("Machado de Assis", resultado.get().getAuthors().get(0));
        assertEquals(256,                resultado.get().getPageCount());
        assertEquals("Atica",            resultado.get().getPublisher());
        assertEquals("1899",             resultado.get().getPublishedDate());
        assertNotNull(resultado.get().getCategories());
        assertFalse(resultado.get().getCategories().isEmpty());
    }

    @Test
    @DisplayName("200 OK com múltiplos itens – deve retornar apenas o primeiro")
    void buscarPorTitulo_200OkMultiplosItens_deveRetornarPrimeiro() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/v1/volumes"))
                .withQueryParam("q", equalTo("romance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "totalItems": 2,
                                  "items": [
                                    {"id":"id1","volumeInfo":{"title":"Primeiro Resultado","authors":["Autor A"],"pageCount":100}},
                                    {"id":"id2","volumeInfo":{"title":"Segundo Resultado","authors":["Autor B"],"pageCount":200}}
                                  ]
                                }
                                """)));

        Optional<GoogleBooksClient.BookInfo> resultado =
                client.buscarPorTitulo("romance");

        assertTrue(resultado.isPresent());
        assertEquals("Primeiro Resultado", resultado.get().getTitle());
    }

    // ══════════════════════════════════════════════════════════════
    // 200 OK – lista vazia / sem items
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("200 OK com lista vazia – deve retornar Optional vazio")
    void buscarPorTitulo_200OkSemItens_deveRetornarVazio() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/v1/volumes"))
                .withQueryParam("q", equalTo("livro xyz inexistente"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"totalItems": 0, "items": []}
                                """)));

        Optional<GoogleBooksClient.BookInfo> resultado =
                client.buscarPorTitulo("livro xyz inexistente");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("200 OK sem campo items – deve retornar Optional vazio")
    void buscarPorTitulo_200OkSemCampoItems_deveRetornarVazio() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/v1/volumes"))
                .withQueryParam("q", equalTo("sem items"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"totalItems": 0}
                                """)));

        Optional<GoogleBooksClient.BookInfo> resultado =
                client.buscarPorTitulo("sem items");

        assertFalse(resultado.isPresent());
    }

    // ══════════════════════════════════════════════════════════════
    // 404 Not Found
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("404 Not Found – deve retornar Optional vazio sem lançar exceção")
    void buscarPorTitulo_404_deveRetornarVazio() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/v1/volumes"))
                .withQueryParam("q", equalTo("titulo nao existe"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error":{"code":404,"message":"Not Found"}}
                                """)));

        Optional<GoogleBooksClient.BookInfo> resultado =
                client.buscarPorTitulo("titulo nao existe");

        assertFalse(resultado.isPresent(),
                "404 deve retornar Optional vazio sem lançar exceção");
    }

    // ══════════════════════════════════════════════════════════════
    // 500 Internal Server Error
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("500 Server Error – deve retornar Optional vazio sem lançar exceção")
    void buscarPorTitulo_500_deveRetornarVazio() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/v1/volumes"))
                .withQueryParam("q", equalTo("erro servidor"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        Optional<GoogleBooksClient.BookInfo> resultado =
                client.buscarPorTitulo("erro servidor");

        assertFalse(resultado.isPresent());
    }

    // ══════════════════════════════════════════════════════════════
    // Resposta com atraso
    // ══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Resposta com atraso de 500ms – cliente deve processar normalmente")
    void buscarPorTitulo_respostaComAtraso_clienteDeveProcessar() {
        wireMockServer.stubFor(get(urlPathEqualTo("/books/v1/volumes"))
                .withQueryParam("q", equalTo("resposta lenta"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(500)
                        .withBody("""
                                {
                                  "totalItems": 1,
                                  "items": [{
                                    "id": "slow1",
                                    "volumeInfo": {
                                      "title": "Livro Lento",
                                      "authors": ["Autor Demorado"]
                                    }
                                  }]
                                }
                                """)));

        Optional<GoogleBooksClient.BookInfo> resultado =
                client.buscarPorTitulo("resposta lenta");

        assertTrue(resultado.isPresent());
        assertEquals("Livro Lento", resultado.get().getTitle());
    }
}