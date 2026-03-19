package com.qs.biblioteca.controller;

import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.service.OpenLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private OpenLibraryService openLibraryService;

    
    @GetMapping
    public List<Livro> listarTodos() {
        return livroRepository.findAll();
    }

    
    @GetMapping("/externos/{categoria}")
    public List<Livro> buscarExternos(@PathVariable String categoria) {
        return openLibraryService.buscarPorCategoria(categoria);
    }

    @GetMapping("/{id}")
public Livro buscarPorId(@PathVariable String id) {
   
    return livroRepository.findById(id)
        .orElseGet(() -> {
         
            return null; 
        });
}

    @PostMapping
    public Livro salvarLivro(@RequestBody Livro livro) {
        return livroRepository.save(livro);
    }
}