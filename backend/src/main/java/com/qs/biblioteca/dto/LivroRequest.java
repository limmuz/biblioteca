package com.qs.biblioteca.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LivroRequest {
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
