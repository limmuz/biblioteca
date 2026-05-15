package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Livro;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class LivroResumoResponse {
    private String id;
    private String title;
    private String author;
    private String cover;
    private String status;
    private List<String> categories;
    private String excerpt;
    private String language;
    private Integer pages;
    private String publisher;
    private String publishedDate;
    private Set<String> camposProtegidos;
    private String criadorEmail;
    private String userEmail;

    public LivroResumoResponse(Livro l) {
        this.id = l.getId();
        this.title = l.getTitle();
        this.author = l.getAuthor();
        this.cover = l.getCover();
        this.status = l.getStatus();
        this.categories = l.getCategories();
        this.excerpt = l.getExcerpt();
        this.language = l.getLanguage();
        this.pages = l.getPages();
        this.publisher = l.getPublisher();
        this.publishedDate = l.getPublishedDate();
        this.camposProtegidos = l.getCamposProtegidos();
        this.criadorEmail = l.getCriadorEmail();
        this.userEmail = l.getUserEmail();
    }
}
