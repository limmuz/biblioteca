package com.qs.biblioteca.service;

import com.qs.biblioteca.dto.AuthRequest;
import com.qs.biblioteca.dto.AuthResponse;
import com.qs.biblioteca.dto.RegisterRequest;
import com.qs.biblioteca.dto.UsuarioResponse;
import com.qs.biblioteca.model.Role;
import com.qs.biblioteca.model.Usuario;
import com.qs.biblioteca.repository.UsuarioRepository;
import com.qs.biblioteca.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public UsuarioService(UsuarioRepository usuarioRepository,
						  PasswordEncoder passwordEncoder,
						  JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public AuthResponse registrar(RegisterRequest request) {
		if (usuarioRepository.existsByEmail(request.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
		}

		Usuario usuario = new Usuario();
		usuario.setNome(request.getNome());
		usuario.setEmail(request.getEmail().toLowerCase());
		usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));
		usuario.setRole(Role.USER);
		usuario.setCep(request.getCep());
		usuario.setLogradouro(request.getLogradouro());
		usuario.setBairro(request.getBairro());
		usuario.setCidade(request.getCidade());
		usuario.setUf(request.getUf());

		Usuario salvo = usuarioRepository.save(usuario);
		String token = jwtService.generateToken(
				salvo.getEmail(),
				Map.of("nome", salvo.getNome(), "role", salvo.getRole().name())
		);

		return new AuthResponse(token, salvo.getNome(), salvo.getEmail());
	}

	public AuthResponse autenticar(AuthRequest request) {
		Usuario usuario = usuarioRepository.findByEmail(request.getEmail().toLowerCase())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

		if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
		}

		String token = jwtService.generateToken(
				usuario.getEmail(),
				Map.of("nome", usuario.getNome(), "role", usuario.getRole().name())
		);

		return new AuthResponse(token, usuario.getNome(), usuario.getEmail());
	}

	public UsuarioResponse buscarPorEmail(String email) {
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				usuario.getRole(),
				usuario.getCep(),
				usuario.getLogradouro(),
				usuario.getBairro(),
				usuario.getCidade(),
				usuario.getUf()
		);
	}
}
