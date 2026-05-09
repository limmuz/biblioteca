package com.qs.biblioteca.controller;

import com.qs.biblioteca.dto.AuthRequest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UsuarioService usuarioService;

	public AuthController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(usuarioService.registrar(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
		return ResponseEntity.ok(usuarioService.autenticar(request));
	}

	@PostMapping("/redefinir-senha")
	public ResponseEntity<Void> redefinirSenha(@RequestBody Map<String, String> body) {
		String email = body.get("email");
		String novaSenha = body.get("novaSenha");
		if (email == null || email.isBlank() || novaSenha == null || novaSenha.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email e nova senha são obrigatórios");
		}
		usuarioService.redefinirSenha(email, novaSenha);
		return ResponseEntity.noContent().build();
	}
}
