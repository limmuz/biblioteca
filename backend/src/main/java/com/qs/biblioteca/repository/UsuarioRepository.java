package com.qs.biblioteca.repository;

import com.qs.biblioteca.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
	Optional<Usuario> findByEmail(String email);
	boolean existsByEmail(String email);
	Optional<Usuario> findByNicknameIgnoreCase(String nickname);
	List<Usuario> findByNomeContainingIgnoreCase(String nome);
}
