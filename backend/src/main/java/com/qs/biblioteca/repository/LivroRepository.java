package com.qs.biblioteca.repository;

import com.qs.biblioteca.model.Livro;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends MongoRepository<Livro, String> {
	List<Livro> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
}