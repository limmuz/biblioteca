package com.qs.biblioteca.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "notificacoes")
public class Notificacao {

    @Id
    private String id;

    private String usuarioEmail;
    private String tipo;
    private String livroTitulo;
    private String livroAutor;
    private String remetente;
    private String atorEmail;
    private String detalhe;
    private String livroId;
    private String livroCover;
    private boolean lida;
    private Instant criadaEm;
}
