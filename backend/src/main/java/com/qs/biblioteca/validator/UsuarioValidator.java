package com.qs.biblioteca.validator;

import com.qs.biblioteca.dto.RegisterRequest;

public final class UsuarioValidator {

    private UsuarioValidator() {}

    public static void validarRegistro(RegisterRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Requisição inválida");
        }

        if (request.getNome() == null || request.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome obrigatório");
        }

        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Email inválido");
        }

        String senha = request.getSenha();

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha obrigatória");
        }

        if (senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 6 caracteres");
        }

        if (senha.matches("\\d+")) {
            throw new IllegalArgumentException("A senha não pode conter apenas números. Use letras e números");
        }

        if (senha.matches("[a-zA-Z]+")) {
            throw new IllegalArgumentException("A senha não pode conter apenas letras. Use letras e números");
        }

        if (!senha.matches(".*[a-zA-Z].*") || !senha.matches(".*\\d.*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra e um número");
        }
    }
}
