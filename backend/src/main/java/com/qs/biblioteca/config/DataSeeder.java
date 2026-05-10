package com.qs.biblioteca.config;

import com.qs.biblioteca.model.Avaliacao;
import com.qs.biblioteca.model.Livro;
import com.qs.biblioteca.model.Role;
import com.qs.biblioteca.model.Usuario;
import com.qs.biblioteca.repository.AvaliacaoRepository;
import com.qs.biblioteca.repository.LivroRepository;
import com.qs.biblioteca.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final String SENHA_PADRAO = "senha123";
    private static final String CATEGORIA_FANTASIA = "Fantasia";
    private static final Random RANDOM = new Random();

    private final UsuarioRepository usuarioRepository;
    private final LivroRepository livroRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      LivroRepository livroRepository,
                      AvaliacaoRepository avaliacaoRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.livroRepository = livroRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByEmail("sofia@leitor.com")) return;

        Usuario sofia = criarUsuario("Sofia Martins", "sofia@leitor.com", SENHA_PADRAO,
                "sofia_reads", "Apaixonada por ficção científica e fantasia ✨");
        Usuario lucas = criarUsuario("Lucas Andrade", "lucas@leitor.com", SENHA_PADRAO,
                "lucas_books", "Leitor de thrillers e mistério 🔍");
        Usuario isabela = criarUsuario("Isabela Costa", "isabela@leitor.com", SENHA_PADRAO,
                "bela_leitora", "Romance e literatura brasileira são minha vida 🌸");

        Livro hp1 = criarLivro(sofia, "Harry Potter e a Pedra Filosofal", "J.K. Rowling",
                "https://covers.openlibrary.org/b/id/7984916-L.jpg",
                "Um garoto descobre que é um bruxo e vai estudar na escola de magia Hogwarts.",
                "LIDO", List.of(CATEGORIA_FANTASIA, "Aventura"));
        criarAvaliacao(hp1, sofia, 5, "Um clássico que nunca envelhece! Li três vezes e cada vez é melhor.");

        Livro duna1 = criarLivro(sofia, "Duna", "Frank Herbert",
                "https://covers.openlibrary.org/b/id/8231432-L.jpg",
                "Em um futuro distante, Paul Atreides luta pela sobrevivência no planeta desértico Arrakis.",
                "LIDO", List.of("Ficção Científica"));
        criarAvaliacao(duna1, sofia, 5, "A melhor ficção científica já escrita. Complexo e brilhante.");

        Livro hp2 = criarLivro(lucas, "Harry Potter e a Pedra Filosofal", "J.K. Rowling",
                "https://covers.openlibrary.org/b/id/7984916-L.jpg",
                "Um garoto descobre que é um bruxo e vai estudar na escola de magia Hogwarts.",
                "LIDO", List.of(CATEGORIA_FANTASIA, "Aventura"));
        criarAvaliacao(hp2, lucas, 4, "Ótimo para todas as idades. A magia é contagiante!");

        Livro vento = criarLivro(lucas, "O Nome do Vento", "Patrick Rothfuss",
                "https://covers.openlibrary.org/b/id/8739161-L.jpg",
                "Kvothe, um lendário músico e mágico, narra sua própria história.",
                "LENDO", List.of(CATEGORIA_FANTASIA));
        criarAvaliacao(vento, lucas, 5, "Narrativa impecável. Rothfuss escreve com uma poesia única.");

        Livro duna2 = criarLivro(isabela, "Duna", "Frank Herbert",
                "https://covers.openlibrary.org/b/id/8231432-L.jpg",
                "Em um futuro distante, Paul Atreides luta pela sobrevivência no planeta desértico Arrakis.",
                "LIDO", List.of("Ficção Científica"));
        criarAvaliacao(duna2, isabela, 4, "Difícil no começo mas incrível no final. Vale muito a leitura.");

        Livro orgulho = criarLivro(isabela, "Orgulho e Preconceito", "Jane Austen",
                "https://covers.openlibrary.org/b/id/8739546-L.jpg",
                "Elizabeth Bennet navega pelas questões de classe social, casamento e moralidade.",
                "LIDO", List.of("Romance", "Clássico"));
        criarAvaliacao(orgulho, isabela, 5, "Perfeito em todos os sentidos. Darcy é eterno! 💕");
    }

    private Usuario criarUsuario(String nome, String email, String senha,
                                  String nickname, String bio) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenhaHash(passwordEncoder.encode(senha));
        u.setNickname(nickname);
        u.setBio(bio);
        u.setRole(Role.USER);
        u.setCep("01310100");
        u.setLogradouro("Av. Paulista");
        u.setBairro("Bela Vista");
        u.setCidade("São Paulo");
        u.setUf("SP");
        return usuarioRepository.save(u);
    }

    private Livro criarLivro(Usuario usuario, String titulo, String autor,
                              String cover, String sinopse,
                              String status, List<String> categorias) {
        Livro livro = new Livro();
        livro.setUserEmail(usuario.getEmail());
        livro.setTitle(titulo);
        livro.setAuthor(autor);
        livro.setCover(cover);
        livro.setExcerpt(sinopse);
        livro.setStatus(status);
        livro.setCategories(categorias);
        livro.setLanguage("Português");
        livro.setPages(400);
        livro.setPublisher("Não informado");
        livro.setPublishedDate("Não informado");
        return livroRepository.save(livro);
    }

    private void criarAvaliacao(Livro livro, Usuario usuario, int rating, String comentario) {
        Avaliacao av = new Avaliacao();
        av.setLivroTitulo(livro.getTitle());
        av.setLivroAutor(livro.getAuthor());
        av.setUsuarioEmail(usuario.getEmail());
        av.setUsuarioNome(usuario.getNome());
        av.setUsuarioNickname(usuario.getNickname());
        av.setRating(rating);
        av.setComentario(comentario);
        av.setCriadoEm(LocalDateTime.now().minusDays(RANDOM.nextLong(30)));
        avaliacaoRepository.save(av);
    }
}
