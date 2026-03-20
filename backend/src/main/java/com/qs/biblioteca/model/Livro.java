package com.qs.biblioteca.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "livros")
public class Livro {
    @Id
    private String id;
    private String title;
    private String author;
    private String cover;
    private String excerpt;
    private String status; // "LIDO", "LENDO", "RECOMENDADO"

    // NOVOS CAMPOS
    private String language; // idioma do livro
    private Integer pages;   // número de páginas
}