package com.qs.biblioteca.integration;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.service.LivroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@Tag("docker")
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integração – LivroRepository com MongoDB real (Testcontainers)")
class LivroRepositoryIntegrationTest extends BaseMongoTest {

    @Autowired
    private LivroRepository livroRepository;

    @Autowired
    private LivroService livroService;

    @BeforeEach
    void limparBanco() {
        livroRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve gerar ID ao salvar um novo livro")
    void salvar_deveGerarId() {
        Livro livroParaSalvar = Objects.requireNonNull(novoLivro("Dom Casmurro", "Machado de Assis"));
        
        Livro salvo = livroRepository.save(livroParaSalvar);

        assertNotNull(salvo.getId());
        assertFalse(salvo.getId().isBlank());
    }

    @Test
    @DisplayName("Deve retornar livro existente ao buscar por ID")
    void findById_deveRetornarLivroExistente() {
        Livro livroParaSalvar = Objects.requireNonNull(novoLivro("1984", "George Orwell"));
        Livro salvo = livroRepository.save(livroParaSalvar);

        String id = Objects.requireNonNull(salvo.getId());
        
        Livro resultado = livroRepository.findById(id)
                .orElseThrow();

        assertEquals("1984", resultado.getTitle());
    }

    @Test
    @DisplayName("Deve esvaziar o repositório ao chamar deleteAll")
    void deleteAll_deveEsvaziarRepositorio() {
        livroRepository.save(Objects.requireNonNull(novoLivro("L1", "A1")));
        livroRepository.save(Objects.requireNonNull(novoLivro("L2", "A2")));

        livroRepository.deleteAll();

        assertEquals(0, livroRepository.count());
    }

    @Test
    @DisplayName("Deve enriquecer metadados de livro incompleto com dados de outra cópia")
    void buscarPorId_deveEnriquecerMetadados() {
        Livro completo = new Livro();
        completo.setUserEmail("outro@integracao.com");
        completo.setTitle("Duna");
        completo.setAuthor("Frank Herbert");
        completo.setStatus("LIDO");
        completo.setPages(600);
        completo.setCover("https://example.com/duna.jpg");
        completo.setExcerpt("A épica saga de Arrakis");
        completo.setLanguage("Português");
        completo.setPublisher("Aleph");
        completo.setPublishedDate("1965-08-01");
        livroRepository.save(completo);

        Livro incompleto = new Livro();
        incompleto.setUserEmail("dono@integracao.com");
        incompleto.setTitle("Duna");
        incompleto.setAuthor("Frank Herbert");
        incompleto.setStatus("QUERO LER");
        incompleto.setPages(600);
        incompleto.setCover("https://example.com/duna.jpg");
        Livro salvo = livroRepository.save(incompleto);

        String id = Objects.requireNonNull(salvo.getId());
        Livro resultado = livroService.buscarPorId(id);

        assertEquals("A épica saga de Arrakis", resultado.getExcerpt());
        assertEquals("Português", resultado.getLanguage());
        assertEquals("Aleph", resultado.getPublisher());
        assertEquals("1965-08-01", resultado.getPublishedDate());
    }

    private static Livro novoLivro(String titulo, String autor) {
        Livro l = new Livro();
        l.setUserEmail("teste@integracao.com");
        l.setTitle(titulo);
        l.setAuthor(autor);
        l.setStatus("QUERO LER");
        l.setPages(200);
        l.setCover("https://example.com/capa.jpg");
        l.setExcerpt("Sinopse de " + titulo);
        return l;
    }
}
