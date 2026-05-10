package com.qs.biblioteca.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "avaliacoes")
public class Avaliacao {

    @Id
    private String id;

    private String livroTitulo;
    private String livroAutor;

    private String usuarioEmail;
    private String usuarioNome;
    private String usuarioNickname;
    private String avatarBase64;

    private int rating;
    private String comentario;

    private LocalDateTime criadoEm = LocalDateTime.now();
}
