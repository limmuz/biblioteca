package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Usuario;
import lombok.Data;

@Data
public class PublicUsuarioResponse {
    private String id;
    private String nome;
    private String nickname;
    private String bio;
    private String avatarBase64;
    private String bgBase64;
    private boolean perfilPublico;

    public PublicUsuarioResponse(Usuario u) {
        this.id = u.getId();
        this.nome = u.getNome();
        this.nickname = u.getNickname();
        this.bio = u.getBio();
        this.avatarBase64 = u.getAvatarBase64();
        this.bgBase64 = u.getBgBase64();
        this.perfilPublico = u.isPerfilPublico();
    }
}
