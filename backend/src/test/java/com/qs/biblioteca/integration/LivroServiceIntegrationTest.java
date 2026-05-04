package com.qs.biblioteca.integration;

import com.qs.biblioteca.TestcontainersConfiguration;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Integração – LivroRepository com MongoDB real (Testcontainers)")
class LivroRepositoryIntegrationTest {

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void limparBanco() {
        livroRepository.deleteAll();
    }

    @Test
    void salvar_deveGerarId() {
        Livro salvo = livroRepository.save(novoLivro("Dom Casmurro", "Machado de Assis"));

        assertNotNull(salvo.getId());
        assertFalse(salvo.getId().isBlank());
    }

    @Test
    void findById_deveRetornarLivroExistente() {
        Livro salvo = livroRepository.save(novoLivro("1984", "George Orwell"));

        Livro resultado = livroRepository.findById(salvo.getId())
                .orElseThrow();

        assertEquals("1984", resultado.getTitle());
    }

    @Test
    void deleteAll_deveEsvaziarRepositorio() {
        livroRepository.save(novoLivro("L1", "A1"));
        livroRepository.save(novoLivro("L2", "A2"));

        livroRepository.deleteAll();

        assertEquals(0, livroRepository.count());
    }

    private static Livro novoLivro(String titulo, String autor) {
        Livro l = new Livro();
        l.setTitle(titulo);
        l.setAuthor(autor);
        l.setStatus("QUERO LER");
        l.setPages(200);
        l.setCover("https://example.com/capa.jpg");
        l.setExcerpt("Sinopse de " + titulo);
        return l;
    }
}