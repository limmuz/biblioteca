package com.qs.biblioteca.integration;

import com.qs.biblioteca.BaseMongoTest;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.model.Notificacao;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.NotificacaoRepository;
import com.qs.biblioteca.service.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("docker")
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integração – NotificacaoService com MongoDB real (Testcontainers)")
class NotificacaoServiceIntegrationTest extends BaseMongoTest {

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private LivroRepository livroRepository;

    @BeforeEach
    void limpar() {
        notificacaoRepository.deleteAll();
        livroRepository.deleteAll();
    }

    @Test
    @DisplayName("notificarUsuario deve salvar notificação no banco")
    void notificarUsuario_devePersistirNotificacao() {
        notificacaoService.notificarUsuario(
                "user@email.com", "AVALIACAO",
                "Dom Casmurro", "Machado de Assis",
                "@leitor", "leitor@email.com", "https://capa.jpg");

        List<Notificacao> lista = notificacaoRepository
                .findByUsuarioEmailOrderByCriadaEmDesc("user@email.com");

        assertEquals(1, lista.size());
        assertEquals("AVALIACAO", lista.get(0).getTipo());
        assertFalse(lista.get(0).isLida());
    }

    @Test
    @DisplayName("listar deve enriquecer capa quando notificação não tem cover mas livro existe")
    void listar_semCover_deveEnriquecerCapaComLivroDoMongo() {
        Livro livro = new Livro();
        livro.setTitle("O Cortiço");
        livro.setAuthor("Aluísio Azevedo");
        livro.setCover("https://example.com/cortico.jpg");
        livro.setUserEmail("outro@email.com");
        livroRepository.save(livro);

        Notificacao n = new Notificacao();
        n.setUsuarioEmail("leitor@email.com");
        n.setTipo("AVALIACAO");
        n.setLivroTitulo("O Cortiço");
        n.setLivroAutor("Aluísio Azevedo");
        n.setLivroCover(null);
        n.setLivroId(null);
        n.setLida(false);
        n.setCriadaEm(Instant.now());
        notificacaoRepository.save(n);

        List<Notificacao> lista = notificacaoService.listarParaUsuario("leitor@email.com");

        assertFalse(lista.isEmpty());
        assertEquals("https://example.com/cortico.jpg", lista.get(0).getLivroCover());
        assertNotNull(lista.get(0).getLivroId());
    }

    @Test
    @DisplayName("listar não deve enriquecer capa quando não há livro correspondente")
    void listar_semLivroCorrespondente_naoEnriqueceCapa() {
        Notificacao n = new Notificacao();
        n.setUsuarioEmail("leitor@email.com");
        n.setTipo("AVALIACAO");
        n.setLivroTitulo("Livro Inexistente");
        n.setLivroAutor("Autor Desconhecido");
        n.setLivroCover(null);
        n.setLivroId(null);
        n.setLida(false);
        n.setCriadaEm(Instant.now());
        notificacaoRepository.save(n);

        List<Notificacao> lista = notificacaoService.listarParaUsuario("leitor@email.com");

        assertFalse(lista.isEmpty());
        assertNull(lista.get(0).getLivroCover());
    }

    @Test
    @DisplayName("excluir não deve remover notificação de outro usuário")
    void excluir_comEmailDiferente_naoExcluiNotificacao() {
        Notificacao n = new Notificacao();
        n.setUsuarioEmail("dono@email.com");
        n.setTipo("AVALIACAO");
        n.setLida(false);
        n.setCriadaEm(Instant.now());
        Notificacao salva = notificacaoRepository.save(n);

        notificacaoService.excluir(salva.getId(), "outro@email.com");

        assertTrue(notificacaoRepository.existsById(salva.getId()));
    }

    @Test
    @DisplayName("excluir deve remover notificação do próprio usuário")
    void excluir_comEmailCorreto_removeNotificacao() {
        Notificacao n = new Notificacao();
        n.setUsuarioEmail("dono@email.com");
        n.setTipo("AVALIACAO");
        n.setLida(false);
        n.setCriadaEm(Instant.now());
        Notificacao salva = notificacaoRepository.save(n);

        notificacaoService.excluir(salva.getId(), "dono@email.com");

        assertFalse(notificacaoRepository.existsById(salva.getId()));
    }
}
