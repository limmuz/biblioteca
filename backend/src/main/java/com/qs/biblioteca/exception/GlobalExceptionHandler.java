package com.qs.biblioteca.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message.isBlank() ? "Dados inválidos" : message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            org.springframework.web.server.ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : "Erro na requisição";
        Map<String, Object> body = new HashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(STATUS, ex.getStatusCode().value());
        body.put(ERROR, ex.getReason() != null ? ex.getReason() : "Erro");
        body.put(MESSAGE, reason);
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {  // NOSONAR
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Algo deu errado");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put(TIMESTAMP, LocalDateTime.now());
        body.put(STATUS, status.value());
        body.put(ERROR, error);
        body.put(MESSAGE, message);
        return ResponseEntity.status(status).body(body);
    }
}
