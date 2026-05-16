package com.qs.biblioteca.controller;

import com.qs.biblioteca.model.Notificacao;
import com.qs.biblioteca.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public List<Notificacao> listar(Authentication authentication) {
        return notificacaoService.listarParaUsuario(authentication.getName());
    }

    @GetMapping("/contagem")
    public Map<String, Long> contagem(Authentication authentication) {
        long naoLidas = notificacaoService.contarNaoLidas(authentication.getName());
        return Map.of("naoLidas", naoLidas);
    }

    @PutMapping("/marcar-lidas")
    public ResponseEntity<Void> marcarLidas(Authentication authentication) {
        notificacaoService.marcarTodasLidas(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @NonNull String id, Authentication authentication) {
        notificacaoService.excluir(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
