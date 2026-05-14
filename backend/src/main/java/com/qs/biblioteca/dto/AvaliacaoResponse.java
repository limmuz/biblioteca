package com.qs.biblioteca.dto;

import com.qs.biblioteca.model.Avaliacao;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AvaliacaoResponse {

    private String id;
    private String livroTitulo;
    private String livroAutor;
    private String livroId;
    private String livroCover;
    private String usuarioNome;
    private String usuarioNickname;
    private String avatarBase64;
    private int rating;
    private String comentario;
    private LocalDateTime criadoEm;
    private boolean minha;
    private int totalCurtidas;
    private boolean euCurti;
    private List<RespostaResponse> respostas;

    public AvaliacaoResponse(Avaliacao a, String emailLogado) {
        this.id = a.getId();
        this.livroTitulo = a.getLivroTitulo();
        this.livroAutor = a.getLivroAutor();
        this.livroId = a.getLivroId();
        this.livroCover = a.getLivroCover();
        this.usuarioNome = a.getUsuarioNome();
        this.usuarioNickname = a.getUsuarioNickname();
        this.avatarBase64 = a.getAvatarBase64();
        this.rating = a.getRating();
        this.comentario = a.getComentario();
        this.criadoEm = a.getCriadoEm();
        this.minha = a.getUsuarioEmail().equalsIgnoreCase(emailLogado);
        List<String> curtidas = a.getCurtidas() != null ? a.getCurtidas() : List.of();
        this.totalCurtidas = curtidas.size();
        this.euCurti = curtidas.stream().anyMatch(e -> e.equalsIgnoreCase(emailLogado));
        this.respostas = a.getRespostas() != null
                ? a.getRespostas().stream().map(r -> new RespostaResponse(r, emailLogado)).toList()
                : List.of();
    }

    @Data
    public static class RespostaResponse {
        private String id;
        private String usuarioNome;
        private String usuarioNickname;
        private String avatarBase64;
        private String texto;
        private LocalDateTime criadoEm;
        private boolean minha;

        public RespostaResponse(Avaliacao.Resposta r, String emailLogado) {
            this.id = r.getId();
            this.usuarioNome = r.getUsuarioNome();
            this.usuarioNickname = r.getUsuarioNickname();
            this.avatarBase64 = r.getAvatarBase64();
            this.texto = r.getTexto();
            this.criadoEm = r.getCriadoEm();
            this.minha = r.getUsuarioEmail().equalsIgnoreCase(emailLogado);
        }
    }
}
