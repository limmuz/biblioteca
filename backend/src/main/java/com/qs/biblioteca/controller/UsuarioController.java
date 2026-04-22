package com.qs.biblioteca.controller;

import com.qs.biblioteca.dto.UsuarioResponse;
import com.qs.biblioteca.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/me")
	public ResponseEntity<UsuarioResponse> me(Authentication authentication) {
		if (authentication == null || authentication.getName() == null) {
			return ResponseEntity.status(401).build();
		}

		return ResponseEntity.ok(usuarioService.buscarPorEmail(authentication.getName()));
	}
}
