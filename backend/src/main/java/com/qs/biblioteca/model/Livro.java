package com.qs.biblioteca.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "livros")
public class Livro {

    @Id
    private String id;

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
}