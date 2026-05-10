package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Avaliacao;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AvaliacaoResponse {

    private String id;
    private String usuarioNome;
    private String usuarioNickname;
    private String avatarBase64;
    private int rating;
    private String comentario;
    private LocalDateTime criadoEm;
    private boolean minha;

    public AvaliacaoResponse(Avaliacao a, String emailLogado) {
        this.id = a.getId();
        this.usuarioNome = a.getUsuarioNome();
        this.usuarioNickname = a.getUsuarioNickname();
        this.avatarBase64 = a.getAvatarBase64();
        this.rating = a.getRating();
        this.comentario = a.getComentario();
        this.criadoEm = a.getCriadoEm();
        this.minha = a.getUsuarioEmail().equalsIgnoreCase(emailLogado);
    }
}
