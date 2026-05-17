package com.qs.biblioteca.unit;

import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.validator.UsuarioValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unitário – UsuarioValidator")
class UsuarioValidatorTest {

    private RegisterRequest requestValido() {
        RegisterRequest req = new RegisterRequest();
        req.setNome("Ana Paula");
        req.setEmail("ana@email.com");
        req.setSenha("senha123");
        return req;
    }

    @Test
    @DisplayName("validarRegistro com request nula lanca IllegalArgumentException")
    void validarRegistro_requestNula_lancaException() {
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(null));
    }

    @Test
    @DisplayName("validarRegistro com nome nulo lanca IllegalArgumentException")
    void validarRegistro_nomeNulo_lancaException() {
        RegisterRequest req = requestValido();
        req.setNome(null);
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com nome em branco lanca IllegalArgumentException")
    void validarRegistro_nomeEmBranco_lancaException() {
        RegisterRequest req = requestValido();
        req.setNome("   ");
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com email nulo lanca IllegalArgumentException")
    void validarRegistro_emailNulo_lancaException() {
        RegisterRequest req = requestValido();
        req.setEmail(null);
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com email sem arroba lanca IllegalArgumentException")
    void validarRegistro_emailSemArroba_lancaException() {
        RegisterRequest req = requestValido();
        req.setEmail("emailsemarroba.com");
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com senha nula lanca IllegalArgumentException")
    void validarRegistro_senhaNula_lancaException() {
        RegisterRequest req = requestValido();
        req.setSenha(null);
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com senha em branco lanca IllegalArgumentException")
    void validarRegistro_senhaEmBranco_lancaException() {
        RegisterRequest req = requestValido();
        req.setSenha("   ");
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com senha muito curta lanca IllegalArgumentException")
    void validarRegistro_senhaCurta_lancaException() {
        RegisterRequest req = requestValido();
        req.setSenha("ab1");
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com senha so numeros lanca IllegalArgumentException")
    void validarRegistro_senhaSoNumeros_lancaException() {
        RegisterRequest req = requestValido();
        req.setSenha("123456");
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com senha so letras lanca IllegalArgumentException")
    void validarRegistro_senhaSoLetras_lancaException() {
        RegisterRequest req = requestValido();
        req.setSenha("abcdefg");
        assertThrows(IllegalArgumentException.class,
                () -> UsuarioValidator.validarRegistro(req));
    }

    @Test
    @DisplayName("validarRegistro com dados validos nao lanca excecao")
    void validarRegistro_dadosValidos_naoLancaException() {
        assertDoesNotThrow(() -> UsuarioValidator.validarRegistro(requestValido()));
    }
}
