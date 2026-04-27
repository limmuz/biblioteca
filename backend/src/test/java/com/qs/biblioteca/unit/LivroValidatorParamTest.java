package com.qs.biblioteca.unit;

import com.qs.biblioteca.model.Livro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários de caixa branca para {@link LivroValidator}.
 *
 * <p><strong>Regras respeitadas:</strong></p>
 * <ul>
 *   <li>Sem {@code @SpringBootTest} – teste puro de lógica de negócio.</li>
 *   <li>Sem Mockito / {@code @Mock} / {@code @MockBean} – instâncias reais.</li>
 *   <li>Usa exclusivamente JUnit 5 ({@code org.junit.jupiter.api}).</li>
 *   <li>Todos os cenários exercitados com {@code @ParameterizedTest}.</li>
 * </ul>
 */
@DisplayName("LivroValidator – testes parametrizados de validação de negócio")
class LivroValidatorParamTest {

    // ══════════════════════════════════════════════════════════════════════
    // Título
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isTituloValido()")
    class TituloTests {

        @ParameterizedTest(name = "[{index}] título={0} → esperado={1}")
        @CsvSource({
                "Dom Casmurro,                        true",
                "O Senhor dos Anéis,                  true",
                "1984,                                true",
                "A,                                   true",
                "'  texto com espaços internos  ',    true"
        })
        @DisplayName("Títulos preenchidos devem ser válidos")
        void tituloPreeenchido_deveSerValido(String titulo, boolean esperado) {
            assertEquals(esperado, LivroValidator.isTituloValido(titulo));
        }

        @ParameterizedTest(name = "[{index}] título em branco/nulo → inválido")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Título nulo, vazio ou somente espaços deve ser inválido")
        void tituloNuloOuBranco_deveSerInvalido(String titulo) {
            assertFalse(LivroValidator.isTituloValido(titulo));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Autor
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isAutorValido()")
    class AutorTests {

        @ParameterizedTest(name = "[{index}] autor={0} → esperado={1}")
        @CsvSource({
                "Machado de Assis,   true",
                "J.R.R. Tolkien,     true",
                "George Orwell,      true",
                "A,                  true"
        })
        @DisplayName("Autores preenchidos devem ser válidos")
        void autorPreenchido_deveSerValido(String autor, boolean esperado) {
            assertEquals(esperado, LivroValidator.isAutorValido(autor));
        }

        @ParameterizedTest(name = "[{index}] autor em branco/nulo → inválido")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Autor nulo, vazio ou somente espaços deve ser inválido")
        void autorNuloOuBranco_deveSerInvalido(String autor) {
            assertFalse(LivroValidator.isAutorValido(autor));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Status
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isStatusValido()")
    class StatusTests {

        @ParameterizedTest(name = "[{index}] status={0} → válido")
        @ValueSource(strings = {
                "QUERO LER", "LENDO", "LIDO", "RECOMENDADO",   // maiúsculo
                "quero ler", "lendo", "lido", "recomendado",   // minúsculo
                "Lendo", "Lido", "Recomendado"                 // mixed-case
        })
        @DisplayName("Status pertencente ao domínio (case-insensitive) deve ser válido")
        void statusDoDominio_deveSerValido(String status) {
            assertTrue(LivroValidator.isStatusValido(status));
        }

        @ParameterizedTest(name = "[{index}] status={0} → inválido")
        @NullAndEmptySource
        @ValueSource(strings = {
                "ABANDONADO", "PAUSADO", "INVALIDO",
                 "READING", "123", "   "
        })
        @DisplayName("Status fora do domínio ou nulo/vazio deve ser inválido")
        void statusForaDoDominio_deveSerInvalido(String status) {
            assertFalse(LivroValidator.isStatusValido(status));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Páginas
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isPaginasValido()")
    class PaginasTests {

        @ParameterizedTest(name = "[{index}] páginas={0} → válido")
        @ValueSource(ints = {1, 50, 300, 1000, Integer.MAX_VALUE})
        @DisplayName("Número de páginas positivo deve ser válido")
        void paginasPositivas_devemSerValidas(int paginas) {
            assertTrue(LivroValidator.isPaginasValido(paginas));
        }

        @ParameterizedTest(name = "[{index}] páginas={0} → inválido")
        @ValueSource(ints = {0, -1, -100, Integer.MIN_VALUE})
        @DisplayName("Zero ou negativo deve ser inválido")
        void paginasZeroOuNegativo_devemSerInvalidas(int paginas) {
            assertFalse(LivroValidator.isPaginasValido(paginas));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("Páginas nulas são permitidas (campo opcional)")
        void paginasNulas_saoPermitidas(Integer paginas) {
            assertTrue(LivroValidator.isPaginasValido(paginas));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Cover (URL da capa)
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isCoverValido()")
    class CoverTests {

        @ParameterizedTest(name = "[{index}] cover={0} → válido")
        @ValueSource(strings = {
                "https://example.com/capa.jpg",
                "http://cdn.livros.com/img/abc.png",
                "https://books.google.com/thumbnail?id=123"
        })
        @DisplayName("URL de capa com protocolo http/https deve ser válida")
        void coverComProtocolo_deveSerValida(String cover) {
            assertTrue(LivroValidator.isCoverValido(cover));
        }

        @ParameterizedTest(name = "[{index}] cover inválida={0}")
        @NullAndEmptySource
        @ValueSource(strings = {
                "ftp://servidor.com/img.jpg",
                "capa.jpg",
                "//sem-protocolo.com/img.jpg",
                "   "
        })
        @DisplayName("URL sem protocolo http/https ou nula/vazia deve ser inválida")
        void coverSemProtocolo_deveSerInvalida(String cover) {
            assertFalse(LivroValidator.isCoverValido(cover));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Livro completo – via @MethodSource
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("isLivroValido() – validação completa do objeto")
    class LivroCompletoTests {

        static Stream<Object[]> livrosParaTestar() {
            return Stream.of(
                    // ── casos válidos ──────────────────────────────────────────────────
                    new Object[]{
                            livro("Dom Casmurro", "Machado de Assis", "LIDO", 256),
                            true,
                            "Livro completo e válido"
                    },
                    new Object[]{
                            livro("1984", "George Orwell", "QUERO LER", null),
                            true,
                            "Páginas nulas são permitidas"
                    },
                    new Object[]{
                            livro("Duna", "Frank Herbert", "lendo", 896),
                            true,
                            "Status em minúsculo deve ser aceito"
                    },

                    // ── título inválido ────────────────────────────────────────────────
                    new Object[]{
                            livro(null, "Machado de Assis", "LIDO", 256),
                            false,
                            "Título nulo deve tornar o livro inválido"
                    },
                    new Object[]{
                            livro("   ", "Machado de Assis", "LIDO", 256),
                            false,
                            "Título em branco deve tornar o livro inválido"
                    },
                    new Object[]{
                            livro("", "Machado de Assis", "LIDO", 256),
                            false,
                            "Título vazio deve tornar o livro inválido"
                    },

                    // ── autor inválido ─────────────────────────────────────────────────
                    new Object[]{
                            livro("Dom Casmurro", null, "LIDO", 256),
                            false,
                            "Autor nulo deve tornar o livro inválido"
                    },
                    new Object[]{
                            livro("Dom Casmurro", "  ", "LIDO", 256),
                            false,
                            "Autor em branco deve tornar o livro inválido"
                    },

                    // ── status inválido ────────────────────────────────────────────────
                    new Object[]{
                            livro("Dom Casmurro", "Machado de Assis", "ABANDONADO", 256),
                            false,
                            "Status fora do domínio deve tornar o livro inválido"
                    },
                    new Object[]{
                            livro("Dom Casmurro", "Machado de Assis", null, 256),
                            false,
                            "Status nulo deve tornar o livro inválido"
                    },

                    // ── páginas inválidas ──────────────────────────────────────────────
                    new Object[]{
                            livro("Dom Casmurro", "Machado de Assis", "LIDO", 0),
                            false,
                            "Páginas = 0 deve tornar o livro inválido"
                    },
                    new Object[]{
                            livro("Dom Casmurro", "Machado de Assis", "LIDO", -10),
                            false,
                            "Páginas negativas devem tornar o livro inválido"
                    },

                    // ── objeto null ────────────────────────────────────────────────────
                    new Object[]{
                            null,
                            false,
                            "Livro null deve ser inválido"
                    }
            );
        }

        @ParameterizedTest(name = "[{index}] {2}")
        @MethodSource("livrosParaTestar")
        @DisplayName("Validação completa do objeto Livro com múltiplos cenários")
        void validacaoCompleta(Livro livro, boolean esperado, String descricao) {
            assertEquals(esperado, LivroValidator.isLivroValido(livro),
                    "Falhou no cenário: " + descricao);
        }
    }

    // ── factory auxiliar ──────────────────────────────────────────────────

    private static Livro livro(String titulo, String autor, String status, Integer paginas) {
        Livro l = new Livro();
        l.setTitle(titulo);
        l.setAuthor(autor);
        l.setStatus(status);
        l.setPages(paginas);
        return l;
    }
}