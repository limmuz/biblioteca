package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioResponse {
    private String id;
    private String nome;
    private String email;
    private Role role;
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;
}