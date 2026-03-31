package com.qs.biblioteca.controller;

import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/livros")
public class LivroController {

    @Autowired
    private LivroRepository livroRepository;


    @GetMapping
    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable String id) {
        return livroRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Livro> salvarLivro(@RequestBody Livro livro) {
        if (livro.getTitle() == null || livro.getTitle().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Livro salvo = livroRepository.save(livro);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(@PathVariable String id, @RequestBody Livro livroAtualizado) {
        return livroRepository.findById(id)
                .map(l -> {
                    l.setStatus(livroAtualizado.getStatus());
                    l.setTitle(livroAtualizado.getTitle());
                    l.setAuthor(livroAtualizado.getAuthor());
                    l.setCover(livroAtualizado.getCover());
                    l.setExcerpt(livroAtualizado.getExcerpt());
                    l.setPages(livroAtualizado.getPages());
                    l.setLanguage(livroAtualizado.getLanguage());
                    Livro salvo = livroRepository.save(l);
                    return ResponseEntity.ok(salvo);
                }).orElse(ResponseEntity.notFound().build());
    }
}