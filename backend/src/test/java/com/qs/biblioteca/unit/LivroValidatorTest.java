package com.qs.biblioteca.unit;

import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.validator.LivroValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unitário – LivroValidator")
class LivroValidatorTest {

    @Test
    @DisplayName("isTituloValido retorna false para null")
    void isTituloValido_nulo_retornaFalse() {
        assertFalse(LivroValidator.isTituloValido(null));
    }

    @ParameterizedTest(name = "[{index}] titulo em branco \"{0}\"")
    @ValueSource(strings = {"", "   "})
    @DisplayName("isTituloValido retorna false para branco")
    void isTituloValido_branco_retornaFalse(String titulo) {
        assertFalse(LivroValidator.isTituloValido(titulo));
    }

    @Test
    @DisplayName("isTituloValido retorna true para titulo valido")
    void isTituloValido_valido_retornaTrue() {
        assertTrue(LivroValidator.isTituloValido("Dom Casmurro"));
    }

    @Test
    @DisplayName("isAutorValido retorna false para null")
    void isAutorValido_nulo_retornaFalse() {
        assertFalse(LivroValidator.isAutorValido(null));
    }

    @ParameterizedTest(name = "[{index}] autor em branco \"{0}\"")
    @ValueSource(strings = {"", "  "})
    @DisplayName("isAutorValido retorna false para branco")
    void isAutorValido_branco_retornaFalse(String autor) {
        assertFalse(LivroValidator.isAutorValido(autor));
    }

    @Test
    @DisplayName("isAutorValido retorna true para autor valido")
    void isAutorValido_valido_retornaTrue() {
        assertTrue(LivroValidator.isAutorValido("Machado de Assis"));
    }

    @Test
    @DisplayName("isStatusValido retorna false para null")
    void isStatusValido_nulo_retornaFalse() {
        assertFalse(LivroValidator.isStatusValido(null));
    }

    @ParameterizedTest(name = "[{index}] status em branco \"{0}\"")
    @ValueSource(strings = {"", "  "})
    @DisplayName("isStatusValido retorna false para branco")
    void isStatusValido_branco_retornaFalse(String status) {
        assertFalse(LivroValidator.isStatusValido(status));
    }

    @Test
    @DisplayName("isStatusValido retorna false para status desconhecido")
    void isStatusValido_invalido_retornaFalse() {
        assertFalse(LivroValidator.isStatusValido("LIXO"));
    }

    @ParameterizedTest(name = "[{index}] status valido \"{0}\"")
    @ValueSource(strings = {"QUERO LER", "LENDO", "LIDO", "RECOMENDADO", "lido", "  lendo  "})
    @DisplayName("isStatusValido retorna true para status validos")
    void isStatusValido_valido_retornaTrue(String status) {
        assertTrue(LivroValidator.isStatusValido(status));
    }

    @Test
    @DisplayName("isPaginasValido retorna true para null")
    void isPaginasValido_nulo_retornaTrue() {
        assertTrue(LivroValidator.isPaginasValido(null));
    }

    @Test
    @DisplayName("isPaginasValido retorna false para zero")
    void isPaginasValido_zero_retornaFalse() {
        assertFalse(LivroValidator.isPaginasValido(0));
    }

    @Test
    @DisplayName("isPaginasValido retorna false para negativo")
    void isPaginasValido_negativo_retornaFalse() {
        assertFalse(LivroValidator.isPaginasValido(-1));
    }

    @Test
    @DisplayName("isPaginasValido retorna true para positivo")
    void isPaginasValido_positivo_retornaTrue() {
        assertTrue(LivroValidator.isPaginasValido(300));
    }

    @Test
    @DisplayName("isCoverValido retorna false para null")
    void isCoverValido_nulo_retornaFalse() {
        assertFalse(LivroValidator.isCoverValido(null));
    }

    @ParameterizedTest(name = "[{index}] cover em branco \"{0}\"")
    @ValueSource(strings = {"", "  "})
    @DisplayName("isCoverValido retorna false para branco")
    void isCoverValido_branco_retornaFalse(String cover) {
        assertFalse(LivroValidator.isCoverValido(cover));
    }

    @Test
    @DisplayName("isCoverValido retorna true para URL http")
    void isCoverValido_http_retornaTrue() {
        assertTrue(LivroValidator.isCoverValido("http://example.com/capa.jpg"));
    }

    @Test
    @DisplayName("isCoverValido retorna true para URL https")
    void isCoverValido_https_retornaTrue() {
        assertTrue(LivroValidator.isCoverValido("https://example.com/capa.jpg"));
    }

    @Test
    @DisplayName("isCoverValido retorna true para caminho local com /")
    void isCoverValido_caminhoLocal_retornaTrue() {
        assertTrue(LivroValidator.isCoverValido("/capas/livro.jpg"));
    }

    @Test
    @DisplayName("isCoverValido retorna false para caminho com //")
    void isCoverValido_doubleSlash_retornaFalse() {
        assertFalse(LivroValidator.isCoverValido("//exemplo.com/capa.jpg"));
    }

    @Test
    @DisplayName("isCoverValido retorna false para string invalida")
    void isCoverValido_invalida_retornaFalse() {
        assertFalse(LivroValidator.isCoverValido("arquivo.jpg"));
    }

    @Test
    @DisplayName("isLivroValido retorna false para null")
    void isLivroValido_nulo_retornaFalse() {
        assertFalse(LivroValidator.isLivroValido(null));
    }

    @Test
    @DisplayName("isLivroValido retorna true para livro valido sem cover")
    void isLivroValido_valido_semCover_retornaTrue() {
        Livro livro = new Livro();
        livro.setTitle("O Alquimista");
        livro.setAuthor("Paulo Coelho");
        livro.setStatus("LIDO");
        livro.setPages(200);
        assertTrue(LivroValidator.isLivroValido(livro));
    }

    @Test
    @DisplayName("isLivroValido retorna true para livro valido com cover")
    void isLivroValido_valido_comCover_retornaTrue() {
        Livro livro = new Livro();
        livro.setTitle("O Alquimista");
        livro.setAuthor("Paulo Coelho");
        livro.setStatus("LIDO");
        livro.setPages(200);
        livro.setCover("https://example.com/capa.jpg");
        assertTrue(LivroValidator.isLivroValido(livro));
    }

    @Test
    @DisplayName("isLivroValido retorna false para titulo nulo")
    void isLivroValido_semTitulo_retornaFalse() {
        Livro livro = new Livro();
        livro.setAuthor("Paulo Coelho");
        livro.setStatus("LIDO");
        assertFalse(LivroValidator.isLivroValido(livro));
    }

    @Test
    @DisplayName("isLivroValido retorna false para status invalido")
    void isLivroValido_statusInvalido_retornaFalse() {
        Livro livro = new Livro();
        livro.setTitle("Livro");
        livro.setAuthor("Autor");
        livro.setStatus("INVALIDO");
        assertFalse(LivroValidator.isLivroValido(livro));
    }

    @Test
    @DisplayName("isLivroValido retorna false para cover invalida")
    void isLivroValido_coverInvalida_retornaFalse() {
        Livro livro = new Livro();
        livro.setTitle("Livro");
        livro.setAuthor("Autor");
        livro.setStatus("LIDO");
        livro.setCover("arquivo.jpg");
        assertFalse(LivroValidator.isLivroValido(livro));
    }
}
