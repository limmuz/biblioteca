package com.qs.biblioteca.repository;

import com.qs.biblioteca.model.Livro;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LivroRepository extends MongoRepository<Livro, String> {

    List<Livro> findByUserEmail(String userEmail);

    @Query("{ 'userEmail': ?0, 'title': { '$regex': ?1, '$options': 'i' } }")
    List<Livro> findByUserEmailAndSearch(String userEmail, String search);

    @Query("{ '$or': [ { 'author': { '$regex': ?0, '$options': 'i' } }, { 'categories': { '$in': ?1 } } ] }")
    List<Livro> findRecomendadosByAutorOrCategorias(String autor, List<String> categorias);

    boolean existsByUserEmailAndTitleIgnoreCaseAndAuthorIgnoreCase(String userEmail, String title, String author);

    @Query("{ 'title': { '$regex': ?0, '$options': 'i' }, 'author': { '$regex': ?1, '$options': 'i' }, 'userEmail': { '$ne': ?2 } }")
    List<Livro> findOutrosLeitoresPorTituloEAutor(String titulo, String autor, String emailAtual);

    @Query("{ 'author': { '$regex': ?0, '$options': 'i' }, 'userEmail': { '$ne': ?1 } }")
    List<Livro> findOutrosLeitoresPorAutor(String autor, String emailAtual);

    @Query("{ 'criadorEmail': ?0, 'userEmail': { '$ne': ?1 } }")
    List<Livro> findByCriadorEmailAndUserEmailNot(String criadorEmail, String emailAtual);

    List<Livro> findByCriadorEmail(String criadorEmail);

    @Query(value = "{ 'title': { '$regex': ?0, '$options': 'i' }, 'author': { '$regex': ?1, '$options': 'i' }, 'cover': { '$exists': true, '$nin': [null, ''] } }", fields = "{ 'cover': 1 }")
    List<Livro> findCapasPorTituloEAutor(String titulo, String autor);

    @Aggregation(pipeline = {
        "{ '$match': { 'userEmail': { '$ne': ?0 }, 'cover': { '$exists': true, '$nin': [null, ''] } } }",
        "{ '$sample': { 'size': 20 } }"
    })
    List<Livro> findDescobrir(String userEmail);
}
