package com.qs.biblioteca.controller;

import com.qs.biblioteca.dto.AvaliacaoRequest;
import com.qs.biblioteca.dto.AvaliacaoResponse;
import com.qs.biblioteca.dto.MediaAvaliacaoResponse;
import com.qs.biblioteca.dto.PublicUsuarioResponse;
import com.qs.biblioteca.service.AvaliacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/livro/{livroId}")
    public ResponseEntity<List<AvaliacaoResponse>> listar(
            @PathVariable String livroId,
            Authentication authentication) {
        return ResponseEntity.ok(avaliacaoService.listarPorLivro(livroId, authentication.getName()));
    }

    @PostMapping("/livro/{livroId}")
    public ResponseEntity<AvaliacaoResponse> avaliar(
            @PathVariable String livroId,
            @Valid @RequestBody AvaliacaoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(avaliacaoService.criarOuAtualizar(authentication.getName(), livroId, request));
    }

    @DeleteMapping("/livro/{livroId}")
    public ResponseEntity<Void> excluir(
            @PathVariable String livroId,
            Authentication authentication) {
        avaliacaoService.excluir(livroId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medias")
    public ResponseEntity<List<MediaAvaliacaoResponse>> medias() {
        return ResponseEntity.ok(avaliacaoService.calcularMedias());
    }

    @GetMapping("/livro/{livroId}/leitores")
    public ResponseEntity<List<PublicUsuarioResponse>> outrosLeitores(
            @PathVariable String livroId,
            Authentication authentication) {
        return ResponseEntity.ok(avaliacaoService.outrosLeitores(livroId, authentication.getName()));
    }
}
