package com.qs.biblioteca.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.annotation.Transient;

@Getter
@Setter
@Document(collection = "livros")
@CompoundIndex(name = "idx_livro_user_title_author",
    def = "{'userEmail': 1, 'title': 1, 'author': 1}")
public class Livro {

    @Id
    private String id;

    @Indexed
    private String userEmail;
    private String title;
    private String author;
    private String cover;
    private String excerpt;
    private String status;
    private String language;
    private Integer pages;
    private List<String> categories;
    private String publisher;
    private String publishedDate;
    private Set<String> camposProtegidos = new HashSet<>();
    private String criadorEmail;

    @Transient
    private Set<String> camposProtegidosPorCriador = new HashSet<>();

    @Transient
    private String criadorNickname;
}