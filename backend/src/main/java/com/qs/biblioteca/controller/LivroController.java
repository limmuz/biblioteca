package com.qs.biblioteca.controller;

import com.qs.biblioteca.dto.LivroRequest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.service.LivroService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
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
        return livroService.listarTodos(search, authentication.getName());
    }

    @GetMapping("/descobrir")
    public List<Livro> descobrir(Authentication authentication) {
        return livroService.descobrir(authentication.getName());
    }

    @GetMapping("/comunidade")
    public List<Livro> comunidade(Authentication authentication) {
        return livroService.comunidade(authentication.getName());
    }

    @GetMapping("/nao-tenho")
    public List<Livro> naoTenho(Authentication authentication) {
        return livroService.naoTenho(authentication.getName());
    }

    @GetMapping("/recomendados")
    public List<Livro> buscarRecomendados(
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) List<String> categorias,
            @RequestParam(required = false) String titulo
    ) {
        return livroService.buscarRecomendados(autor, categorias, titulo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(livroService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Livro> salvarLivro(
            @RequestBody LivroRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        Livro entity = toEntity(request);
        if (entity.getCriadorEmail() == null) entity.setCriadorEmail(email);
        return ResponseEntity.status(201).body(livroService.salvar(entity, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(
            @PathVariable String id,
            @RequestBody LivroRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(livroService.atualizar(id, toEntity(request), authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerLivro(
            @PathVariable String id,
            Authentication authentication
    ) {
        livroService.deletar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> excluirPermanente(
            @PathVariable String id,
            Authentication authentication
    ) {
        livroService.deletarPermanente(id, authentication.getName());
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
        if (r.getCamposProtegidos() != null) livro.setCamposProtegidos(r.getCamposProtegidos());
        if (r.getCriadorEmail() != null) livro.setCriadorEmail(r.getCriadorEmail());
        return livro;
    }
}
