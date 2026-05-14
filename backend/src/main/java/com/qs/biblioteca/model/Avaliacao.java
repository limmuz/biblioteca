package com.qs.biblioteca.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "avaliacoes")
@CompoundIndexes({
    @CompoundIndex(name = "idx_av_titulo_autor",
        def = "{'livroTitulo': 1, 'livroAutor': 1}"),
    @CompoundIndex(name = "idx_av_titulo_autor_email",
        def = "{'livroTitulo': 1, 'livroAutor': 1, 'usuarioEmail': 1}",
        unique = true)
})
public class Avaliacao {

    @Id
    private String id;

    private String livroTitulo;
    private String livroAutor;
    private String livroId;
    private String livroCover;

    @Indexed
    private String usuarioEmail;
    private String usuarioNome;
    private String usuarioNickname;
    private String avatarBase64;

    private int rating;
    private String comentario;

    private LocalDateTime criadoEm = LocalDateTime.now();

    private List<String> curtidas = new ArrayList<>();
    private List<Resposta> respostas = new ArrayList<>();

    @Getter
    @Setter
    public static class Resposta {
        private String id = UUID.randomUUID().toString();
        private String usuarioEmail;
        private String usuarioNome;
        private String usuarioNickname;
        private String avatarBase64;
        private String texto;
        private LocalDateTime criadoEm = LocalDateTime.now();
    }
}
