package com.qs.biblioteca.service;

import com.qs.biblioteca.dto.AvaliacaoRequest;
import com.qs.biblioteca.dto.AvaliacaoResponse;
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

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
                            LivroRepository livroRepository,
                            UsuarioRepository usuarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<AvaliacaoResponse> listarPorLivro(String livroId, String emailLogado) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
        return avaliacaoRepository
                .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCase(livro.getTitle(), livro.getAuthor())
                .stream()
                .map(a -> new AvaliacaoResponse(a, emailLogado))
                .toList();
    }

    public AvaliacaoResponse criarOuAtualizar(String email, String livroId, AvaliacaoRequest req) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        Avaliacao avaliacao = avaliacaoRepository
                .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCaseAndUsuarioEmail(
                        livro.getTitle(), livro.getAuthor(), email)
                .orElse(new Avaliacao());

        avaliacao.setLivroTitulo(livro.getTitle());
        avaliacao.setLivroAutor(livro.getAuthor());
        avaliacao.setUsuarioEmail(email);
        avaliacao.setUsuarioNome(usuario.getNome());
        avaliacao.setUsuarioNickname(usuario.getNickname());
        avaliacao.setAvatarBase64(usuario.getAvatarBase64());
        avaliacao.setRating(req.getRating());
        avaliacao.setComentario(req.getComentario());
        avaliacao.setCriadoEm(LocalDateTime.now());

        return new AvaliacaoResponse(avaliacaoRepository.save(avaliacao), email);
    }

    public List<PublicUsuarioResponse> outrosLeitores(String livroId, String emailAtual) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
        return livroRepository
                .findOutrosLeitoresPorTituloEAutor(livro.getTitle(), livro.getAuthor(), emailAtual)
                .stream()
                .map(l -> usuarioRepository.findByEmail(l.getUserEmail()).orElse(null))
                .filter(u -> u != null)
                .distinct()
                .limit(6)
                .map(PublicUsuarioResponse::new)
                .toList();
    }

    public void excluir(String livroId, String email) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
        Avaliacao avaliacao = avaliacaoRepository
                .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCaseAndUsuarioEmail(
                        livro.getTitle(), livro.getAuthor(), email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avaliação não encontrada"));
        avaliacaoRepository.delete(avaliacao);
    }
}
