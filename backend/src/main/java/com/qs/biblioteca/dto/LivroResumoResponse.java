package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Livro;
import lombok.Data;

import java.util.List;

@Data
public class LivroResumoResponse {
    private String title;
    private String author;
    private String cover;
    private String status;
    private List<String> categories;

    public LivroResumoResponse(Livro l) {
        this.title = l.getTitle();
        this.author = l.getAuthor();
        this.cover = l.getCover();
        this.status = l.getStatus();
        this.categories = l.getCategories();
    }
}
