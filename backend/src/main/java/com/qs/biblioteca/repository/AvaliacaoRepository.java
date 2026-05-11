package com.qs.biblioteca.repository;

import com.qs.biblioteca.model.Avaliacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends MongoRepository<Avaliacao, String> {

    List<Avaliacao> findByUsuarioEmail(String email);

    List<Avaliacao> findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCase(String titulo, String autor);

    Optional<Avaliacao> findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCaseAndUsuarioEmail(
            String titulo, String autor, String email);
}
