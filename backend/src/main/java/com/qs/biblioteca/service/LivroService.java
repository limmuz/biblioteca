package com.qs.biblioteca.service;

import com.qs.biblioteca.exception.ResourceNotFoundException;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.validator.LivroValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public Livro salvar(Livro livro) {
        validarLivro(livro);
        if (livro.getTitle() != null && livro.getAuthor() != null
                && livroRepository.existsByTitleIgnoreCaseAndAuthorIgnoreCase(
                        livro.getTitle().trim(), livro.getAuthor().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Livro já cadastrado: \"" + livro.getTitle() + "\" de " + livro.getAuthor());
        }
        return livroRepository.save(livro);
    }

    public List<Livro> listarTodos(String search) {
        if (search != null && !search.isBlank()) {
            return livroRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search, search);
        }
        return livroRepository.findAll();
    }

    public Livro buscarPorId(String id) {
        return livroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado"));
    }

    public Livro atualizar(String id, Livro livroAtualizado) {
        Livro livro = buscarPorId(id);

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

        return livroRepository.save(livro);
    }

    public void deletar(String id) {
        if (!livroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livro não encontrado");
        }
        livroRepository.deleteById(id);
    }

    private void validarLivro(Livro livro) {
        if (!LivroValidator.isLivroValido(livro)) {
            throw new IllegalArgumentException("Dados do livro inválidos");
        }
    }
}