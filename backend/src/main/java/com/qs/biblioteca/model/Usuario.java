package com.qs.biblioteca.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nome;

    private String nickname;

    @Indexed(unique = true)
    private String email;

    private String senhaHash;

    private Role role = Role.USER;

    // Endereço único (legado)
    private String cep;
    private String logradouro;
    private String bairro;
    private String cidade;
    private String uf;

    // Múltiplos endereços
    private List<Endereco> enderecos;

    // Múltiplos telefones
    private List<String> telefones;

    // Redes sociais
    private List<String> redesSociais;

    @Getter
    @Setter
    public static class Endereco {
        private String cep;
        private String logradouro;
        private String numero;
        private String complemento;
        private String bairro;
        private String cidade;
        private String uf;
    }
}
