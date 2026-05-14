package com.qs.biblioteca.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RespostaRequest {

    @NotBlank
    @Size(max = 500)
    private String texto;
}
