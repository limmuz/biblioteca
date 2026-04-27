package com.qs.biblioteca.integration;

import com.qs.biblioteca.TestcontainersConfiguration;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de integração: LivroRepository ↔ MongoDB real (Testcontainers).
 * Sem Mockito. Banco real via MongoDBContainer com @ServiceConnection.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("Integração – LivroRepository com MongoDB real (Testcontainers)")
class LivroServiceIntegrationTest {

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void limparBanco() {
        livroRepository.deleteAll();
    }

    // ══════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Salvar (CREATE)")
    class SalvarTests {

        @Test
        @DisplayName("Deve persistir livro e gerar ID automaticamente")
        void salvar_deveGerarId() {
            Livro salvo = livroRepository.save(novoLivro("Dom Casmurro", "Machado de Assis"));

            assertNotNull(salvo.getId());
            assertFalse(salvo.getId().isBlank());
        }

        @Test
        @DisplayName("Deve persistir todos os campos corretamente")
        void salvar_devePersistirTodosOsCampos() {
            Livro livro = new Livro();
            livro.setTitle("O Senhor dos Aneis");
            livro.setAuthor("J.R.R. Tolkien");
            livro.setStatus("QUERO LER");
            livro.setLanguage("Portugues");
            livro.setPages(1200);
            livro.setPublisher("HarperCollins");
            livro.setPublishedDate("1954-07-29");
            livro.setCategories(List.of("Fantasia", "Aventura"));
            livro.setCover("https://books.google.com/capa.jpg");
            livro.setExcerpt("A jornada de Frodo Baggins...");

            Livro recuperado = livroRepository.findById(livroRepository.save(livro).getId()).orElseThrow();

            assertEquals("O Senhor dos Aneis",           recuperado.getTitle());
            assertEquals("J.R.R. Tolkien",                recuperado.getAuthor());
            assertEquals("QUERO LER",                    recuperado.getStatus());
            assertEquals("Portugues",                    recuperado.getLanguage());
            assertEquals(1200,                           recuperado.getPages());
            assertEquals("HarperCollins",                recuperado.getPublisher());
            assertEquals("1954-07-29",                   recuperado.getPublishedDate());
            assertEquals(List.of("Fantasia", "Aventura"), recuperado.getCategories());
        }

        @Test
        @DisplayName("Deve salvar múltiplos livros independentemente")
        void salvar_devePersistirMultiplosLivros() {
            livroRepository.save(novoLivro("Livro A", "Autor 1"));
            livroRepository.save(novoLivro("Livro B", "Autor 2"));
            livroRepository.save(novoLivro("Livro C", "Autor 3"));

            assertEquals(3, livroRepository.count());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // READ
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Buscar (READ)")
    class BuscarTests {

        @Test
        @DisplayName("findById deve retornar livro existente")
        void findById_deveRetornarLivroExistente() {
            Livro salvo = livroRepository.save(novoLivro("1984", "George Orwell"));

            Optional<Livro> resultado = livroRepository.findById(salvo.getId());

            assertTrue(resultado.isPresent());
            assertEquals("1984", resultado.get().getTitle());
        }

        @Test
        @DisplayName("findById deve retornar Optional vazio para ID inexistente")
        void findById_deveRetornarVazioParaIdInexistente() {
            assertFalse(livroRepository.findById("id-inexistente").isPresent());
        }

        @Test
        @DisplayName("findAll deve retornar todos os livros persistidos")
        void findAll_deveRetornarTodosOsLivros() {
            livroRepository.save(novoLivro("Duna",     "Frank Herbert"));
            livroRepository.save(novoLivro("Fundacao", "Isaac Asimov"));

            assertEquals(2, livroRepository.findAll().size());
        }

        @Test
        @DisplayName("findAll em banco vazio deve retornar lista vazia")
        void findAll_emBancoVazio_deveRetornarListaVazia() {
            assertTrue(livroRepository.findAll().isEmpty());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // SEARCH (query personalizada)
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Busca por título e autor")
    class BuscaPersonalizadaTests {

        @Test
        @DisplayName("Deve encontrar livro pelo título parcial case-insensitive")
        void buscarPorTitulo_parcial_caseInsensitive() {
            livroRepository.save(novoLivro("O Alquimista", "Paulo Coelho"));
            livroRepository.save(novoLivro("Dom Casmurro", "Machado de Assis"));

            List<Livro> resultado = livroRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                            "alquimista", "alquimista");

            assertEquals(1, resultado.size());
            assertEquals("O Alquimista", resultado.get(0).getTitle());
        }

        @Test
        @DisplayName("Deve encontrar livros pelo autor parcial case-insensitive")
        void buscarPorAutor_parcial_caseInsensitive() {
            livroRepository.save(novoLivro("O Alquimista", "Paulo Coelho"));
            livroRepository.save(novoLivro("11 Minutos",   "Paulo Coelho"));
            livroRepository.save(novoLivro("Dom Casmurro", "Machado de Assis"));

            List<Livro> resultado = livroRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("coelho", "coelho");

            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Busca sem correspondência deve retornar lista vazia")
        void buscarSemCorrespondencia_deveRetornarListaVazia() {
            livroRepository.save(novoLivro("O Alquimista", "Paulo Coelho"));

            List<Livro> resultado = livroRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("tolkien", "tolkien");

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Busca retorna união de título OU autor")
        void buscarPorTituloOuAutor_deveRetornarUniao() {
            livroRepository.save(novoLivro("Historias de Asimov", "Autor Qualquer"));
            livroRepository.save(novoLivro("Fundacao",            "Isaac Asimov"));
            livroRepository.save(novoLivro("Duna",                "Frank Herbert"));

            List<Livro> resultado = livroRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("asimov", "asimov");

            assertEquals(2, resultado.size());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // UPDATE
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Atualizar (UPDATE)")
    class AtualizarTests {

        @Test
        @DisplayName("Deve atualizar campos mantendo o mesmo ID")
        void atualizar_deveManterId() {
            Livro original = livroRepository.save(novoLivro("Titulo Original", "Autor Original"));
            String id = original.getId();

            original.setTitle("Titulo Atualizado");
            original.setStatus("LIDO");
            original.setPages(350);
            Livro atualizado = livroRepository.save(original);

            assertEquals(id,                  atualizado.getId());
            assertEquals("Titulo Atualizado", atualizado.getTitle());
            assertEquals("LIDO",              atualizado.getStatus());
            assertEquals(350,                 atualizado.getPages());
        }

        @Test
        @DisplayName("Atualização deve refletir na próxima consulta")
        void atualizar_deveRefletirNaConsulta() {
            Livro salvo = livroRepository.save(novoLivro("Antes", "Autor"));
            salvo.setTitle("Depois");
            livroRepository.save(salvo);

            assertEquals("Depois",
                    livroRepository.findById(salvo.getId()).orElseThrow().getTitle());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // DELETE
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Remover (DELETE)")
    class RemoverTests {

        @Test
        @DisplayName("Deve remover livro e não encontrá-lo mais pelo ID")
        void deletar_deveRemoverDoRepositorio() {
            Livro salvo = livroRepository.save(novoLivro("Para Deletar", "Autor Teste"));
            livroRepository.deleteById(salvo.getId());

            assertFalse(livroRepository.findById(salvo.getId()).isPresent());
        }

        @Test
        @DisplayName("existsById deve retornar false após deleção")
        void deletar_existsByIdDeveRetornarFalse() {
            Livro salvo = livroRepository.save(novoLivro("Efemero", "Autor"));
            assertTrue(livroRepository.existsById(salvo.getId()));
            livroRepository.deleteById(salvo.getId());
            assertFalse(livroRepository.existsById(salvo.getId()));
        }

        @Test
        @DisplayName("deleteAll deve esvaziar o repositório completamente")
        void deleteAll_deveEsvaziarRepositorio() {
            livroRepository.save(novoLivro("L1", "A1"));
            livroRepository.save(novoLivro("L2", "A2"));
            livroRepository.save(novoLivro("L3", "A3"));

            assertEquals(3, livroRepository.count());
            livroRepository.deleteAll();
            assertEquals(0, livroRepository.count());
        }
    }

    // ── factory auxiliar ──────────────────────────────────────────

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