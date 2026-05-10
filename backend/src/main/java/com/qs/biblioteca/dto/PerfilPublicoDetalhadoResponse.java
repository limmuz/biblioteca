package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.model.Usuario;
import lombok.Data;

import java.util.List;

@Data
public class PerfilPublicoDetalhadoResponse {
    private String id;
    private String nome;
    private String nickname;
    private String bio;
    private String avatarBase64;
    private String bgBase64;
    private int totalLidos;
    private int totalLendo;
    private int totalQueroLer;
    private List<LivroResumoResponse> livros;

    public PerfilPublicoDetalhadoResponse(Usuario u, List<Livro> todosLivros) {
        this.id = u.getId();
        this.nome = u.getNome();
        this.nickname = u.getNickname();
        this.bio = u.getBio();
        this.avatarBase64 = u.getAvatarBase64();
        this.bgBase64 = u.getBgBase64();

        List<Livro> publicos = todosLivros.stream()
                .filter(l -> List.of("LIDO", "LENDO", "QUERO LER").contains(l.getStatus()))
                .toList();

        this.livros = publicos.stream().map(LivroResumoResponse::new).toList();
        this.totalLidos = (int) publicos.stream().filter(l -> "LIDO".equals(l.getStatus())).count();
        this.totalLendo = (int) publicos.stream().filter(l -> "LENDO".equals(l.getStatus())).count();
        this.totalQueroLer = (int) publicos.stream().filter(l -> "QUERO LER".equals(l.getStatus())).count();
    }
}
