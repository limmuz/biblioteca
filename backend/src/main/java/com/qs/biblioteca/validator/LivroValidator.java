package com.qs.biblioteca.unit;

import com.qs.biblioteca.model.Livro;
import java.util.Set;

/**
 * Classe auxiliar de validação de negócio usada pelos testes parametrizados.
 * Fica no mesmo pacote (com.qs.biblioteca.unit) que LivroValidatorParamTest,
 * então NÃO precisa de import no arquivo de teste.
 */
public final class LivroValidator {

    private static final Set<String> STATUS_VALIDOS = Set.of(
            "QUERO LER", "LENDO", "LIDO", "RECOMENDADO"
    );

    private LivroValidator() { }

    public static boolean isTituloValido(String titulo) {
        return titulo != null && !titulo.isBlank();
    }

    public static boolean isAutorValido(String autor) {
        return autor != null && !autor.isBlank();
    }

    public static boolean isStatusValido(String status) {
        if (status == null || status.isBlank()) return false;
        return STATUS_VALIDOS.contains(status.trim().toUpperCase());
    }

    public static boolean isPaginasValido(Integer paginas) {
        return paginas == null || paginas > 0;
    }

    public static boolean isCoverValido(String cover) {
        if (cover == null || cover.isBlank()) return false;
        return cover.startsWith("http://") || cover.startsWith("https://");
    }

    public static boolean isLivroValido(Livro livro) {
        if (livro == null) return false;
        return isTituloValido(livro.getTitle())
                && isAutorValido(livro.getAuthor())
                && isStatusValido(livro.getStatus())
                && isPaginasValido(livro.getPages());
    }
}