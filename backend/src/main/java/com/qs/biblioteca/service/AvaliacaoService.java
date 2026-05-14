package com.qs.biblioteca.service;

import com.qs.biblioteca.dto.AvaliacaoRequest;
import com.qs.biblioteca.dto.AvaliacaoResponse;
import com.qs.biblioteca.dto.MediaAvaliacaoResponse;
import com.qs.biblioteca.dto.PublicUsuarioResponse;
import com.qs.biblioteca.model.Avaliacao;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.model.Usuario;
import com.qs.biblioteca.repository.AvaliacaoRepository;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class AvaliacaoService {

    private static final String LIVRO_NAO_ENCONTRADO = "Livro não encontrado";
    private static final String AVALIACAO_NAO_ENCONTRADA = "Avaliação não encontrada";

    private final AvaliacaoRepository avaliacaoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            LivroRepository livroRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<AvaliacaoResponse> listarPorLivro(String livroId, String emailLogado) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, LIVRO_NAO_ENCONTRADO));
        String titulo = Objects.requireNonNull(livro.getTitle());
        String autor = Objects.requireNonNull(livro.getAuthor());
        return avaliacaoRepository
                .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCase(titulo, autor)
                .stream()
                .map(a -> new AvaliacaoResponse(a, emailLogado))
                .toList();
    }

    public AvaliacaoResponse criarOuAtualizar(String email, String livroId, AvaliacaoRequest req) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, LIVRO_NAO_ENCONTRADO));
        Usuario usuario = usuarioRepository.findByEmailParaAvaliacao(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        String titulo = Objects.requireNonNull(livro.getTitle());
        String autor = Objects.requireNonNull(livro.getAuthor());
        Avaliacao avaliacao = avaliacaoRepository
                .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCaseAndUsuarioEmail(titulo, autor, email)
                .orElse(new Avaliacao());
        avaliacao.setLivroTitulo(titulo);
        avaliacao.setLivroAutor(autor);
        avaliacao.setLivroId(livroId);
        avaliacao.setLivroCover(livro.getCover());
        avaliacao.setUsuarioEmail(email);
        avaliacao.setUsuarioNome(usuario.getNome());
        avaliacao.setUsuarioNickname(usuario.getNickname());
        avaliacao.setAvatarBase64(usuario.getAvatarBase64());
        avaliacao.setRating(req.getRating());
        avaliacao.setComentario(req.getComentario());
        avaliacao.setCriadoEm(LocalDateTime.now());
        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        return new AvaliacaoResponse(salva, email);
    }

    public AvaliacaoResponse curtir(String avaliacaoId, String email) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AVALIACAO_NAO_ENCONTRADA));
        if (avaliacao.getUsuarioEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível curtir o próprio comentário");
        }
        List<String> curtidas = avaliacao.getCurtidas();
        boolean jaCurtiu = curtidas.stream().anyMatch(e -> e.equalsIgnoreCase(email));
        if (jaCurtiu) {
            curtidas.removeIf(e -> e.equalsIgnoreCase(email));
        } else {
            curtidas.add(email);
        }
        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        return new AvaliacaoResponse(salva, email);
    }

    public AvaliacaoResponse responder(String avaliacaoId, String email, String texto) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AVALIACAO_NAO_ENCONTRADA));
        Usuario usuario = usuarioRepository.findByEmailParaAvaliacao(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        Avaliacao.Resposta resposta = new Avaliacao.Resposta();
        resposta.setUsuarioEmail(email);
        resposta.setUsuarioNome(usuario.getNome());
        resposta.setUsuarioNickname(usuario.getNickname());
        resposta.setAvatarBase64(usuario.getAvatarBase64());
        resposta.setTexto(texto.trim());
        avaliacao.getRespostas().add(resposta);
        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        return new AvaliacaoResponse(salva, email);
    }

    public void excluirResposta(String avaliacaoId, String respostaId, String email) {
        Avaliacao avaliacao = avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AVALIACAO_NAO_ENCONTRADA));
        Avaliacao.Resposta resposta = avaliacao.getRespostas().stream()
                .filter(r -> r.getId().equals(respostaId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resposta não encontrada"));
        if (!resposta.getUsuarioEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Não é possível excluir a resposta de outro usuário");
        }
        avaliacao.getRespostas().removeIf(r -> r.getId().equals(respostaId));
        avaliacaoRepository.save(avaliacao);
    }

    public List<MediaAvaliacaoResponse> calcularMedias() {
        Map<String, List<Avaliacao>> agrupado = avaliacaoRepository.findAllParaMedias()
                .stream()
                .collect(Collectors.groupingBy(a ->
                        Objects.requireNonNull(a.getLivroTitulo()).toLowerCase()
                                + "||"
                                + Objects.requireNonNull(a.getLivroAutor()).toLowerCase()
                ));
        return agrupado.entrySet()
                .stream()
                .map(e -> {
                    List<Avaliacao> lista = e.getValue();
                    double media = lista.stream().mapToInt(Avaliacao::getRating).average().orElse(0);
                    MediaAvaliacaoResponse r = new MediaAvaliacaoResponse();
                    r.setLivroTitulo(lista.get(0).getLivroTitulo());
                    r.setLivroAutor(lista.get(0).getLivroAutor());
                    r.setMedia(Math.round(media * 10.0) / 10.0);
                    r.setTotal(lista.size());
                    return r;
                })
                .toList();
    }

    public List<PublicUsuarioResponse> outrosLeitores(String livroId, String emailAtual) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, LIVRO_NAO_ENCONTRADO));
        String titulo = Objects.requireNonNull(livro.getTitle());
        String autor = Objects.requireNonNull(livro.getAuthor());
        return livroRepository
                .findOutrosLeitoresPorTituloEAutor(titulo, autor, emailAtual)
                .stream()
                .map(l -> usuarioRepository.findByEmailParaAvaliacao(l.getUserEmail()).orElse(null))
                .filter(Objects::nonNull)
                .distinct()
                .map(PublicUsuarioResponse::new)
                .limit(6)
                .toList();
    }

    public List<AvaliacaoResponse> listarMinhas(String email) {
        return avaliacaoRepository.findByUsuarioEmail(email)
                .stream()
                .map(a -> new AvaliacaoResponse(a, email))
                .toList();
    }

    public void excluir(String livroId, String email) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, LIVRO_NAO_ENCONTRADO));
        String titulo = Objects.requireNonNull(livro.getTitle());
        String autor = Objects.requireNonNull(livro.getAuthor());
        Avaliacao avaliacao = avaliacaoRepository
                .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCaseAndUsuarioEmail(titulo, autor, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AVALIACAO_NAO_ENCONTRADA));
        avaliacaoRepository.delete(avaliacao);
    }
}
