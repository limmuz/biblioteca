package com.qs.biblioteca.dto;

import lombok.Data;

@Data
public class MediaAvaliacaoResponse {
    private String livroTitulo;
    private String livroAutor;
    private double media;
    private long total;
}
