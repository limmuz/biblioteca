package com.qs.biblioteca.service;

import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.model.Notificacao;
import com.qs.biblioteca.model.Usuario;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.NotificacaoRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository, UsuarioRepository usuarioRepository,
            LivroRepository livroRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
    }

    public void notificarSeguidores(String atorEmail, String tipo, String livroTitulo, String livroAutor,
            String detalhe, String livroId, String livroCover) {
        usuarioRepository.findByEmail(atorEmail).ifPresent(ator -> {
            String atorId = ator.getId();
            String nomeAtor = ator.getNickname() != null && !ator.getNickname().isBlank()
                    ? "@" + ator.getNickname() : ator.getNome();
            List<Usuario> seguidores = usuarioRepository.findByLeitoresSeguidosContaining(atorId);
            for (Usuario seguidor : seguidores) {
                salvar(seguidor.getEmail(), tipo, livroTitulo, livroAutor, nomeAtor, atorEmail, detalhe, livroId, livroCover);
            }
        });
    }

    public void notificarUsuario(String usuarioEmail, String tipo, String livroTitulo, String livroAutor,
            String remetente, String atorEmail, String detalhe, String livroId, String livroCover) {
        salvar(usuarioEmail, tipo, livroTitulo, livroAutor, remetente, atorEmail, detalhe, livroId, livroCover);
    }

    public List<Notificacao> listarParaUsuario(String usuarioEmail) {
        List<Notificacao> lista = notificacaoRepository.findByUsuarioEmailOrderByCriadaEmDesc(usuarioEmail);
        for (Notificacao n : lista) {
            boolean semCover = n.getLivroCover() == null || n.getLivroCover().isBlank();
            boolean semId = n.getLivroId() == null || n.getLivroId().isBlank();
            if ((semCover || semId) && n.getLivroTitulo() != null && n.getLivroAutor() != null) {
                List<Livro> capas = livroRepository.findCapasPorTituloEAutor(n.getLivroTitulo(), n.getLivroAutor());
                if (!capas.isEmpty()) {
                    Livro capa = capas.get(0);
                    if (semCover && capa.getCover() != null) n.setLivroCover(capa.getCover());
                    if (semId) n.setLivroId(capa.getId());
                }
            }
        }
        return lista;
    }

    public long contarNaoLidas(String usuarioEmail) {
        return notificacaoRepository.countByUsuarioEmailAndLidaFalse(usuarioEmail);
    }

    public void marcarTodasLidas(String usuarioEmail) {
        List<Notificacao> naoLidas = notificacaoRepository.findByUsuarioEmailAndLidaFalse(usuarioEmail);
        naoLidas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(naoLidas);
    }

    public void excluir(String id, String usuarioEmail) {
        notificacaoRepository.findById(id).ifPresent(n -> {
            if (usuarioEmail.equalsIgnoreCase(n.getUsuarioEmail())) {
                notificacaoRepository.delete(n);
            }
        });
    }

    private void salvar(String usuarioEmail, String tipo, String livroTitulo, String livroAutor,
            String remetente, String atorEmail, String detalhe, String livroId, String livroCover) {
        Notificacao n = new Notificacao();
        n.setUsuarioEmail(usuarioEmail);
        n.setTipo(tipo);
        n.setLivroTitulo(livroTitulo);
        n.setLivroAutor(livroAutor);
        n.setRemetente(remetente);
        n.setAtorEmail(atorEmail);
        n.setDetalhe(detalhe);
        n.setLivroId(livroId);
        n.setLivroCover(livroCover);
        n.setLida(false);
        n.setCriadaEm(Instant.now());
        notificacaoRepository.save(n);
    }
}
