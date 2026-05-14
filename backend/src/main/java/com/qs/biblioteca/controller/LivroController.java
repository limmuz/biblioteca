package com.qs.biblioteca.controller;

import com.qs.biblioteca.dto.LivroRequest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.service.LivroService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/livros")
@SuppressWarnings("null")
public class LivroController {

    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    @GetMapping
    public List<Livro> listarTodos(
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {

        String email = Objects.requireNonNull(authentication.getName());

        return livroService.listarTodos(search, email);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable String id) {

        Livro livro = livroService.buscarPorId(id);

        return ResponseEntity.ok(livro);
    }

    @PostMapping
    public ResponseEntity<Livro> salvarLivro(
            @RequestBody LivroRequest request,
            Authentication authentication
    ) {

        String email = Objects.requireNonNull(authentication.getName());

        Livro salvo = livroService.salvar(toEntity(request), email);

        return ResponseEntity.status(201).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(
            @PathVariable String id,
            @RequestBody LivroRequest request,
            Authentication authentication
    ) {

        String email = Objects.requireNonNull(authentication.getName());

        Livro atualizado = livroService.atualizar(id, toEntity(request), email);

        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerLivro(
            @PathVariable String id,
            Authentication authentication
    ) {

        String email = Objects.requireNonNull(authentication.getName());

        livroService.deletar(id, email);

        return ResponseEntity.noContent().build();
    }

    private static Livro toEntity(LivroRequest r) {

        Livro livro = new Livro();

        livro.setTitle(r.getTitle());
        livro.setAuthor(r.getAuthor());
        livro.setCover(r.getCover());
        livro.setExcerpt(r.getExcerpt());
        livro.setStatus(r.getStatus());
        livro.setLanguage(r.getLanguage());
        livro.setPages(r.getPages());
        livro.setCategories(r.getCategories());
        livro.setPublisher(r.getPublisher());
        livro.setPublishedDate(r.getPublishedDate());

        return livro;
    }
}