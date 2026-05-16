package com.qs.biblioteca.unit;

import com.qs.biblioteca.exception.GlobalExceptionHandler;
import com.qs.biblioteca.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unitário – GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleNotFound deve retornar 404 com corpo de erro")
    void handleNotFound_deveRetornar404() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleNotFound(new ResourceNotFoundException("livro não encontrado"));

        assertEquals(404, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("Not Found", resp.getBody().get("error"));
        assertEquals("livro não encontrado", resp.getBody().get("message"));
    }

    @Test
    @DisplayName("handleBadRequest deve retornar 400 com mensagem da exceção")
    void handleBadRequest_deveRetornar400() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleBadRequest(new IllegalArgumentException("argumento inválido"));

        assertEquals(400, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("argumento inválido", resp.getBody().get("message"));
    }

    @Test
    @DisplayName("handleResponseStatus com reason deve retornar status e mensagem corretos")
    void handleResponseStatus_comReason_deveRetornarMensagemCorreta() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.FORBIDDEN, "acesso negado");

        ResponseEntity<Map<String, Object>> resp = handler.handleResponseStatus(ex);

        assertEquals(403, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("acesso negado", resp.getBody().get("message"));
    }

    @Test
    @DisplayName("handleResponseStatus sem reason deve usar mensagem padrão")
    void handleResponseStatus_semReason_deveUsarMensagemPadrao() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT);

        ResponseEntity<Map<String, Object>> resp = handler.handleResponseStatus(ex);

        assertEquals(409, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("Erro na requisição", resp.getBody().get("message"));
    }

    @Test
    @DisplayName("handleGeneric deve retornar 500 para exceção inesperada")
    void handleGeneric_deveRetornar500() {
        ResponseEntity<Map<String, Object>> resp =
                handler.handleGeneric(new RuntimeException("erro inesperado interno"));

        assertEquals(500, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals("Internal Server Error", resp.getBody().get("error"));
        assertEquals("Algo deu errado", resp.getBody().get("message"));
    }
}
