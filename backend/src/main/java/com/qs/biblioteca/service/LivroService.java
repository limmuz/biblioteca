package com.qs.biblioteca.service;

import com.qs.biblioteca.exception.ResourceNotFoundException;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.repository.AvaliacaoRepository;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import com.qs.biblioteca.validator.LivroValidator;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public LivroService(LivroRepository livroRepository, AvaliacaoRepository avaliacaoRepository,
            UsuarioRepository usuarioRepository, NotificacaoService notificacaoService) {
        this.livroRepository = livroRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    public Livro salvar(
            @NonNull Livro livro,
            @NonNull String userEmail
    ) {

        livro.setUserEmail(userEmail);

        validarLivro(livro);

        if (livro.getTitle() != null
                && livro.getAuthor() != null
                && livroRepository
                .existsByUserEmailAndTitleIgnoreCaseAndAuthorIgnoreCase(
                        userEmail,
                        livro.getTitle().trim(),
                        livro.getAuthor().trim()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Livro já cadastrado: \""
                            + livro.getTitle()
                            + "\" de "
                            + livro.getAuthor()
            );
        }

        Livro salvo = livroRepository.save(livro);
        notificacaoService.notificarSeguidores(userEmail, "LIVRO_CRIADO", salvo.getTitle(), salvo.getAuthor(), null, salvo.getId(), salvo.getCover());
        return salvo;
    }

    public List<Livro> listarTodos(
            String search,
            @NonNull String userEmail
    ) {

        if (search != null && !search.isBlank()) {
            return livroRepository.findByUserEmailAndSearch(userEmail, "^" + search);
        }

        return livroRepository.findByUserEmail(userEmail);
    }

    public Livro buscarPorId(@NonNull String id) {

        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado"));

        if (livro.getTitle() == null || livro.getAuthor() == null) {
            return livro;
        }

        String emailDono = livro.getUserEmail() != null ? livro.getUserEmail() : "";
        List<Livro> outrosCopias = buscarOutrasCopias(livro, emailDono);

        if (metadataIncompleta(livro)) {
            enriquecerMetadata(livro, outrosCopias);
        }

        boolean criadorEraNulo = livro.getCriadorEmail() == null;
        inferirCriadorEmailSeNecessario(livro, outrosCopias, emailDono);
        if (criadorEraNulo && livro.getCriadorEmail() != null) {
            livroRepository.save(livro);
            String criadorInferido = livro.getCriadorEmail();
            for (Livro copia : outrosCopias) {
                if (copia.getCriadorEmail() == null) {
                    copia.setCriadorEmail(criadorInferido);
                    livroRepository.save(copia);
                }
            }
        }

        resolverCamposBloqueadosPorCriador(livro, outrosCopias);
        resolverCriadorNickname(livro);

        return livro;
    }

    private void inferirCriadorEmailSeNecessario(Livro livro, List<Livro> outrosCopias, String emailDono) {
        if (livro.getCriadorEmail() != null) return;
        if (outrosCopias.isEmpty()) {
            livro.setCriadorEmail(emailDono);
            return;
        }
        List<Livro> todas = new ArrayList<>(outrosCopias);
        todas.add(livro);
        todas.stream()
                .filter(l -> l.getId() != null)
                .min(Comparator.comparing(Livro::getId))
                .ifPresent(mais -> livro.setCriadorEmail(mais.getUserEmail()));
        if (livro.getCriadorEmail() == null) {
            livro.setCriadorEmail(emailDono);
        }
    }

    private void resolverCamposBloqueadosPorCriador(Livro livro, List<Livro> outrosCopias) {
        String emailCriador = livro.getCriadorEmail();
        Set<String> bloqueados = outrosCopias.stream()
                .filter(c -> emailCriador != null && emailCriador.equals(c.getUserEmail()))
                .filter(c -> c.getCamposProtegidos() != null)
                .flatMap(c -> c.getCamposProtegidos().stream())
                .collect(Collectors.toSet());
        livro.setCamposProtegidosPorCriador(bloqueados);
    }

    private void resolverCriadorNickname(Livro livro) {
        String emailCriador = livro.getCriadorEmail();
        if (emailCriador == null) return;
        usuarioRepository.findByEmail(emailCriador.toLowerCase())
                .ifPresent(u -> {
                    String nick = u.getNickname();
                    livro.setCriadorNickname(
                            nick != null && !nick.isBlank() ? "@" + nick : u.getNome()
                    );
                });
    }

    private boolean metadataIncompleta(Livro livro) {
        return isBlank(livro.getExcerpt())
                || isBlank(livro.getLanguage())
                || isBlank(livro.getPublisher())
                || isBlank(livro.getPublishedDate());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase().trim();
    }

    private void enriquecerMetadata(Livro destino, List<Livro> fontes) {
        for (Livro fonte : fontes) {
            if (isBlank(destino.getExcerpt()) && !isBlank(fonte.getExcerpt()))
                destino.setExcerpt(fonte.getExcerpt());
            if (isBlank(destino.getLanguage()) && !isBlank(fonte.getLanguage()))
                destino.setLanguage(fonte.getLanguage());
            if (isBlank(destino.getPublisher()) && !isBlank(fonte.getPublisher()))
                destino.setPublisher(fonte.getPublisher());
            if (isBlank(destino.getPublishedDate()) && !isBlank(fonte.getPublishedDate()))
                destino.setPublishedDate(fonte.getPublishedDate());
            if (isBlank(destino.getCover()) && !isBlank(fonte.getCover()))
                destino.setCover(fonte.getCover());
            if ((destino.getCategories() == null || destino.getCategories().isEmpty())
                    && fonte.getCategories() != null && !fonte.getCategories().isEmpty())
                destino.setCategories(fonte.getCategories());
            if (!metadataIncompleta(destino)) break;
        }
    }

    public Livro atualizar(
            @NonNull String id,
            @NonNull Livro livroAtualizado,
            @NonNull String userEmail
    ) {

        Livro livro = buscarPorId(id);

        if (livro.getUserEmail() != null
                && !userEmail.equals(livro.getUserEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado"
            );
        }

        livro.setUserEmail(userEmail);

        String tituloAntes = livro.getTitle();
        String autorAntes = livro.getAuthor();

        livro.setTitle(livroAtualizado.getTitle());
        livro.setAuthor(livroAtualizado.getAuthor());
        livro.setStatus(livroAtualizado.getStatus());
        livro.setCover(livroAtualizado.getCover());
        livro.setExcerpt(livroAtualizado.getExcerpt());
        livro.setPages(livroAtualizado.getPages());
        livro.setLanguage(livroAtualizado.getLanguage());
        livro.setCategories(livroAtualizado.getCategories());
        livro.setPublisher(livroAtualizado.getPublisher());
        livro.setPublishedDate(livroAtualizado.getPublishedDate());
        if (livroAtualizado.getCamposProtegidos() != null) {
            livro.setCamposProtegidos(livroAtualizado.getCamposProtegidos());
        }

        validarLivro(livro);

        Livro salvo = livroRepository.save(livro);

        propagarMetadata(salvo, userEmail, tituloAntes, autorAntes);
        notificacaoService.notificarSeguidores(userEmail, "LIVRO_ATUALIZADO", salvo.getTitle(), salvo.getAuthor(), null, salvo.getId(), salvo.getCover());

        return salvo;
    }

    private List<Livro> buscarOutrasCopias(Livro livro, String emailExcluir) {
        String tituloNorm = normalizar(livro.getTitle());
        if (tituloNorm.isBlank() || isBlank(livro.getAuthor())) return List.of();
        String autorNorm = normalizar(livro.getAuthor());
        String criador = livro.getCriadorEmail();
        if (criador != null && !criador.isBlank()) {
            return livroRepository.findByCriadorEmailAndUserEmailNot(criador, emailExcluir)
                    .stream()
                    .filter(l -> normalizar(l.getTitle()).equals(tituloNorm)
                            && normalizar(l.getAuthor()).equals(autorNorm))
                    .toList();
        }
        return livroRepository.findOutrosLeitoresPorAutor(livro.getAuthor(), emailExcluir)
                .stream()
                .filter(l -> normalizar(l.getTitle()).equals(tituloNorm))
                .toList();
    }

    private List<Livro> copiasDaMesmaObra(Livro salvo, String emailEditor, String tituloOriginal, String autorOriginal) {
        String tituloNorm = normalizar(tituloOriginal != null ? tituloOriginal : salvo.getTitle());
        String tituloAtualNorm = normalizar(salvo.getTitle());
        String autorAtualNorm = normalizar(salvo.getAuthor() != null ? salvo.getAuthor() : "");
        String criador = salvo.getCriadorEmail();
        if (criador != null && !criador.isBlank()) {
            return livroRepository.findByCriadorEmailAndUserEmailNot(criador, emailEditor)
                    .stream()
                    .filter(l -> {
                        String lt = normalizar(l.getTitle());
                        String la = normalizar(l.getAuthor() != null ? l.getAuthor() : "");
                        return (lt.equals(tituloNorm) || lt.equals(tituloAtualNorm)) && la.equals(autorAtualNorm);
                    })
                    .toList();
        }
        String autorBusca = (autorOriginal != null && !autorOriginal.isBlank()) ? autorOriginal : salvo.getAuthor();
        if (tituloNorm.isBlank() || isBlank(autorBusca)) return List.of();
        return livroRepository.findOutrosLeitoresPorAutor(autorBusca, emailEditor)
                .stream()
                .filter(l -> normalizar(l.getTitle()).equals(tituloNorm))
                .toList();
    }

    private void propagarMetadata(Livro salvo, String emailEditor, String tituloOriginal, String autorOriginal) {
        List<Livro> copias = copiasDaMesmaObra(salvo, emailEditor, tituloOriginal, autorOriginal);
        if (copias.isEmpty()) return;
        for (Livro copia : copias) {
            boolean alterou = false;
            java.util.Set<String> prot = copia.getCamposProtegidos() != null
                    ? copia.getCamposProtegidos() : java.util.Set.of();
            if (!prot.contains("cover") && !isBlank(salvo.getCover()) && !salvo.getCover().equals(copia.getCover())) {
                copia.setCover(salvo.getCover());
                alterou = true;
            }
            if (!prot.contains("excerpt") && !isBlank(salvo.getExcerpt()) && !salvo.getExcerpt().equals(copia.getExcerpt())) {
                copia.setExcerpt(salvo.getExcerpt());
                alterou = true;
            }
            if (!prot.contains("language") && !isBlank(salvo.getLanguage()) && !salvo.getLanguage().equals(copia.getLanguage())) {
                copia.setLanguage(salvo.getLanguage());
                alterou = true;
            }
            if (!prot.contains("pages") && salvo.getPages() != null && !salvo.getPages().equals(copia.getPages())) {
                copia.setPages(salvo.getPages());
                alterou = true;
            }
            if (!prot.contains("categories") && salvo.getCategories() != null && !salvo.getCategories().isEmpty()
                    && !salvo.getCategories().equals(copia.getCategories())) {
                copia.setCategories(salvo.getCategories());
                alterou = true;
            }
            if (!prot.contains("publisher") && !isBlank(salvo.getPublisher()) && !salvo.getPublisher().equals(copia.getPublisher())) {
                copia.setPublisher(salvo.getPublisher());
                alterou = true;
            }
            if (!prot.contains("publishedDate") && !isBlank(salvo.getPublishedDate()) && !salvo.getPublishedDate().equals(copia.getPublishedDate())) {
                copia.setPublishedDate(salvo.getPublishedDate());
                alterou = true;
            }
            if (alterou) livroRepository.save(copia);
        }
    }

    public List<Livro> buscarRecomendados(String autor, List<String> categorias, String titulo) {
        String autorPadrao = (autor != null && !autor.isBlank()) ? autor : "";
        List<String> catsPadrao = (categorias != null) ? categorias : List.of();
        if (autorPadrao.isBlank() && catsPadrao.isEmpty()) return List.of();
        String tituloNorm = normalizar(titulo);
        Set<String> vistos = new HashSet<>();
        return livroRepository.findRecomendadosByAutorOrCategorias(autorPadrao, catsPadrao)
                .stream()
                .filter(l -> l.getTitle() != null && !normalizar(l.getTitle()).equals(tituloNorm))
                .filter(l -> l.getCover() != null && !l.getCover().isBlank())
                .filter(l -> vistos.add(chaveIdentidade(l)))
                .limit(10)
                .toList();
    }

    private String chaveIdentidade(Livro l) {
        return normalizar(l.getTitle()) + "||" + normalizar(l.getAuthor());
    }

    public List<Livro> descobrir(String userEmail) {
        Set<String> vistos = new HashSet<>();
        return livroRepository.findDescobrir(userEmail)
                .stream()
                .filter(l -> l.getTitle() != null && l.getCover() != null)
                .filter(l -> vistos.add(chaveIdentidade(l)))
                .limit(12)
                .toList();
    }

    public void deletar(
            @NonNull String id,
            @NonNull String userEmail
    ) {

        Livro livro = buscarPorId(id);

        if (livro.getUserEmail() != null
                && !userEmail.equals(livro.getUserEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado"
            );
        }

        String titulo = livro.getTitle();
        String autor = livro.getAuthor();

        if (titulo != null && autor != null) {
            avaliacaoRepository
                    .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCaseAndUsuarioEmail(
                            titulo, autor, userEmail)
                    .ifPresent(avaliacaoRepository::delete);
        }
        String cover = livro.getCover();
        livroRepository.deleteById(id);
        notificacaoService.notificarSeguidores(userEmail, "LIVRO_EXCLUIDO", titulo, autor, null, null, cover);
    }

    public void deletarPermanente(
            @NonNull String id,
            @NonNull String userEmail
    ) {

        Livro livro = buscarPorId(id);

        if (!userEmail.equals(livro.getCriadorEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas o criador pode excluir permanentemente"
            );
        }

        String titulo = livro.getTitle();
        String autor = livro.getAuthor();
        String cover = livro.getCover();
        String criadorEmail = livro.getCriadorEmail();

        List<Livro> todasCopias = (criadorEmail != null && !criadorEmail.isBlank())
                ? livroRepository.findByCriadorEmail(criadorEmail)
                : List.of(livro);

        String nomeAtor = usuarioRepository.findByEmail(userEmail)
                .map(u -> u.getNickname() != null && !u.getNickname().isBlank()
                        ? "@" + u.getNickname() : u.getNome())
                .orElse(userEmail);

        for (Livro copia : todasCopias) {
            String emailCopia = copia.getUserEmail();
            if (copia.getTitle() != null && copia.getAuthor() != null && emailCopia != null) {
                avaliacaoRepository
                        .findByLivroTituloIgnoreCaseAndLivroAutorIgnoreCaseAndUsuarioEmail(
                                copia.getTitle(), copia.getAuthor(), emailCopia)
                        .ifPresent(avaliacaoRepository::delete);
            }
            if (emailCopia != null && !userEmail.equals(emailCopia)) {
                notificacaoService.notificarUsuario(
                        emailCopia, "LIVRO_EXCLUIDO_PERMANENTE",
                        titulo, autor, nomeAtor, userEmail, null, null, cover);
            }
            String copiaId = copia.getId();
            if (copiaId != null) livroRepository.deleteById(copiaId);
        }

        notificacaoService.notificarSeguidores(userEmail, "LIVRO_EXCLUIDO", titulo, autor, null, null, cover);
    }

    private void validarLivro(@NonNull Livro livro) {

        if (!LivroValidator.isLivroValido(livro)) {
            throw new IllegalArgumentException(
                    "Dados do livro inválidos"
            );
        }
    }
}
