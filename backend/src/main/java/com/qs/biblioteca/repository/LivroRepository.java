package com.qs.biblioteca.repository;

import com.qs.biblioteca.model.Livro;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends MongoRepository<Livro, String> {

    List<Livro> findByUserEmail(String userEmail);

    @Query("{ 'userEmail': ?0, '$or': [ { 'title': { '$regex': ?1, '$options': 'i' } }, { 'author': { '$regex': ?1, '$options': 'i' } } ] }")
    List<Livro> findByUserEmailAndSearch(String userEmail, String search);

    boolean existsByUserEmailAndTitleIgnoreCaseAndAuthorIgnoreCase(String userEmail, String title, String author);
}
