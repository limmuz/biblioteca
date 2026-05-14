package com.qs.biblioteca.service;

import com.qs.biblioteca.exception.ResourceNotFoundException;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.validator.LivroValidator;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public Livro salvar(
            @NonNull Livro livro,
            @NonNull String userEmail
    ) {

        livro.setUserEmail(
                Objects.requireNonNull(userEmail)
        );

        validarLivro(livro);

        if (livro.getTitle() != null
                && livro.getAuthor() != null
                && livroRepository
                .existsByUserEmailAndTitleIgnoreCaseAndAuthorIgnoreCase(
                        userEmail,
                        livro.getTitle().trim(),
                        livro.getAuthor().trim()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Livro já cadastrado: \""
                            + livro.getTitle()
                            + "\" de "
                            + livro.getAuthor()
            );
        }

        return Objects.requireNonNull(
                livroRepository.save(livro)
        );
    }

    public List<Livro> listarTodos(
            String search,
            @NonNull String userEmail
    ) {

        if (search != null && !search.isBlank()) {

            return Objects.requireNonNull(
                    livroRepository.findByUserEmailAndSearch(
                            userEmail,
                            search
                    )
            );
        }

        return Objects.requireNonNull(
                livroRepository.findByUserEmail(userEmail)
        );
    }

    public Livro buscarPorId(@NonNull String id) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado"));

        if (metadataIncompleta(livro) && livro.getTitle() != null && livro.getAuthor() != null) {
            String emailDono = livro.getUserEmail() != null ? livro.getUserEmail() : "";
            List<Livro> outrosCopias = livroRepository.findOutrosLeitoresPorTituloEAutor(
                    livro.getTitle(), livro.getAuthor(), emailDono);
            enriquecerMetadata(livro, outrosCopias);
        }

        return Objects.requireNonNull(livro);
    }

    private boolean metadataIncompleta(Livro livro) {
        return isBlank(livro.getExcerpt())
                || isBlank(livro.getLanguage())
                || isBlank(livro.getPublisher())
                || isBlank(livro.getPublishedDate());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void enriquecerMetadata(Livro destino, List<Livro> fontes) {
        for (Livro fonte : fontes) {
            if (isBlank(destino.getExcerpt()) && !isBlank(fonte.getExcerpt()))
                destino.setExcerpt(fonte.getExcerpt());
            if (isBlank(destino.getLanguage()) && !isBlank(fonte.getLanguage()))
                destino.setLanguage(fonte.getLanguage());
            if (isBlank(destino.getPublisher()) && !isBlank(fonte.getPublisher()))
                destino.setPublisher(fonte.getPublisher());
            if (isBlank(destino.getPublishedDate()) && !isBlank(fonte.getPublishedDate()))
                destino.setPublishedDate(fonte.getPublishedDate());
            if (isBlank(destino.getCover()) && !isBlank(fonte.getCover()))
                destino.setCover(fonte.getCover());
            if ((destino.getCategories() == null || destino.getCategories().isEmpty())
                    && fonte.getCategories() != null && !fonte.getCategories().isEmpty())
                destino.setCategories(fonte.getCategories());
            if (!metadataIncompleta(destino)) break;
        }
    }

    public Livro atualizar(
            @NonNull String id,
            @NonNull Livro livroAtualizado,
            @NonNull String userEmail
    ) {

        Livro livro = buscarPorId(id);

        if (livro.getUserEmail() != null
                && !userEmail.equals(livro.getUserEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado"
            );
        }

        livro.setUserEmail(userEmail);

        livro.setTitle(livroAtualizado.getTitle());
        livro.setAuthor(livroAtualizado.getAuthor());
        livro.setStatus(livroAtualizado.getStatus());
        livro.setCover(livroAtualizado.getCover());
        livro.setExcerpt(livroAtualizado.getExcerpt());
        livro.setPages(livroAtualizado.getPages());
        livro.setLanguage(livroAtualizado.getLanguage());
        livro.setCategories(livroAtualizado.getCategories());
        livro.setPublisher(livroAtualizado.getPublisher());
        livro.setPublishedDate(livroAtualizado.getPublishedDate());

        validarLivro(livro);

        return Objects.requireNonNull(
                livroRepository.save(livro)
        );
    }

    public void deletar(
            @NonNull String id,
            @NonNull String userEmail
    ) {

        Livro livro = buscarPorId(id);

        if (livro.getUserEmail() != null
                && !userEmail.equals(livro.getUserEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado"
            );
        }

        livroRepository.deleteById(id);
    }

    private void validarLivro(@NonNull Livro livro) {

        if (!LivroValidator.isLivroValido(livro)) {
            throw new IllegalArgumentException(
                    "Dados do livro inválidos"
            );
        }
    }
}