package com.qs.biblioteca.repository;

import com.qs.biblioteca.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
	Optional<Usuario> findByEmail(String email);
	boolean existsByEmail(String email);
	Optional<Usuario> findByNicknameIgnoreCase(String nickname);
	List<Usuario> findByNomeContainingIgnoreCase(String nome);

	@Query(value = "{'email': ?0}",
		   fields = "{'avatarBase64': 0, 'bgBase64': 0, 'enderecos': 0, 'telefones': 0, 'redesSociais': 0}")
	Optional<Usuario> findByEmailLeve(String email);

	@Query(value = "{'email': ?0}",
		   fields = "{'bgBase64': 0, 'enderecos': 0, 'telefones': 0, 'redesSociais': 0, 'senhaHash': 0, 'cep': 0, 'logradouro': 0, 'bairro': 0, 'cidade': 0, 'uf': 0}")
	Optional<Usuario> findByEmailParaAvaliacao(String email);

	@Query(value = "{'perfilPublico': true, 'email': {$ne: ?0}}",
		   fields = "{'senhaHash': 0, 'enderecos': 0, 'telefones': 0, 'redesSociais': 0, 'cep': 0, 'logradouro': 0, 'bairro': 0, 'cidade': 0, 'uf': 0}")
	List<Usuario> findPublicosLeves(String emailExcluir);

	@Query(value = "{'nome': {$regex: ?0, $options: 'i'}, 'perfilPublico': true}",
		   fields = "{'senhaHash': 0, 'enderecos': 0, 'telefones': 0, 'redesSociais': 0, 'cep': 0, 'logradouro': 0, 'bairro': 0, 'cidade': 0, 'uf': 0}")
	List<Usuario> findByNomeLeve(String nome);

	@Query(value = "{'nickname': {$regex: ?0, $options: 'i'}, 'perfilPublico': true}",
		   fields = "{'senhaHash': 0, 'enderecos': 0, 'telefones': 0, 'redesSociais': 0, 'cep': 0, 'logradouro': 0, 'bairro': 0, 'cidade': 0, 'uf': 0}")
	List<Usuario> findByNicknameLeve(String nickname);

	List<Usuario> findByLeitoresSeguidosContaining(String userId);
}
