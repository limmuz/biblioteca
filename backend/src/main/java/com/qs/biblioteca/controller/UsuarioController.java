package com.qs.biblioteca.controller;

import com.qs.biblioteca.dto.PerfilPublicoDetalhadoResponse;
import com.qs.biblioteca.dto.PublicUsuarioResponse;
import com.qs.biblioteca.dto.UsuarioResponse;
import com.qs.biblioteca.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/me")
	public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
		return ResponseEntity.ok(
				usuarioService.buscarPorEmail(authentication.getName()));
	}

	@PutMapping("/me")
	public ResponseEntity<UsuarioResponse> atualizar(
			Authentication authentication,
			@RequestBody Map<String, Object> dados) {
		return ResponseEntity.ok(
				usuarioService.atualizarPorEmail(authentication.getName(), dados));
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> excluir(Authentication authentication) {
		usuarioService.excluirPorEmail(authentication.getName());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/leitores")
	public ResponseEntity<List<PublicUsuarioResponse>> leitores(Authentication authentication) {
		return ResponseEntity.ok(usuarioService.listarLeitoresPublicos(authentication.getName()));
	}

	@GetMapping("/buscar")
	public ResponseEntity<PublicUsuarioResponse> buscarPorNickname(
			@RequestParam String nickname) {
		return ResponseEntity.ok(usuarioService.buscarPublicoPorNickname(nickname));
	}

	@GetMapping("/pesquisar")
	public ResponseEntity<List<PublicUsuarioResponse>> pesquisar(@RequestParam String q) {
		return ResponseEntity.ok(usuarioService.buscarPublicoPorTermo(q));
	}

	@GetMapping("/perfil/{nickname}")
	public ResponseEntity<PerfilPublicoDetalhadoResponse> perfilPublico(@PathVariable String nickname) {
		return ResponseEntity.ok(usuarioService.buscarPerfilPublicoDetalhado(nickname));
	}
}
