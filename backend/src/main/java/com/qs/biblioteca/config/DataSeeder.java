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

@Component
public class DataSeeder implements ApplicationRunner {

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

        Usuario sofia = criarUsuario("Sofia Martins", "sofia@leitor.com", "senha123",
                "sofia_reads", "Apaixonada por ficção científica e fantasia ✨");
        Usuario lucas = criarUsuario("Lucas Andrade", "lucas@leitor.com", "senha123",
                "lucas_books", "Leitor de thrillers e mistério 🔍");
        Usuario isabela = criarUsuario("Isabela Costa", "isabela@leitor.com", "senha123",
                "bela_leitora", "Romance e literatura brasileira são minha vida 🌸");

        criarLivroComAvaliacao(sofia, "Harry Potter e a Pedra Filosofal", "J.K. Rowling",
                "https://covers.openlibrary.org/b/id/7984916-L.jpg",
                "Um garoto descobre que é um bruxo e vai estudar na escola de magia Hogwarts.",
                "LIDO", List.of("Fantasia", "Aventura"), 5,
                "Um clássico que nunca envelhece! Li três vezes e cada vez é melhor.");

        criarLivroComAvaliacao(sofia, "Duna", "Frank Herbert",
                "https://covers.openlibrary.org/b/id/8231432-L.jpg",
                "Em um futuro distante, Paul Atreides luta pela sobrevivência no planeta desértico Arrakis.",
                "LIDO", List.of("Ficção Científica"), 5,
                "A melhor ficção científica já escrita. Complexo e brilhante.");

        criarLivroComAvaliacao(lucas, "Harry Potter e a Pedra Filosofal", "J.K. Rowling",
                "https://covers.openlibrary.org/b/id/7984916-L.jpg",
                "Um garoto descobre que é um bruxo e vai estudar na escola de magia Hogwarts.",
                "LIDO", List.of("Fantasia", "Aventura"), 4,
                "Ótimo para todas as idades. A magia é contagiante!");

        criarLivroComAvaliacao(lucas, "O Nome do Vento", "Patrick Rothfuss",
                "https://covers.openlibrary.org/b/id/8739161-L.jpg",
                "Kvothe, um lendário músico e mágico, narra sua própria história.",
                "LENDO", List.of("Fantasia"), 5,
                "Narrativa impecável. Rothfuss escreve com uma poesia única.");

        criarLivroComAvaliacao(isabela, "Duna", "Frank Herbert",
                "https://covers.openlibrary.org/b/id/8231432-L.jpg",
                "Em um futuro distante, Paul Atreides luta pela sobrevivência no planeta desértico Arrakis.",
                "LIDO", List.of("Ficção Científica"), 4,
                "Difícil no começo mas incrível no final. Vale muito a leitura.");

        criarLivroComAvaliacao(isabela, "Orgulho e Preconceito", "Jane Austen",
                "https://covers.openlibrary.org/b/id/8739546-L.jpg",
                "Elizabeth Bennet navega pelas questões de classe social, casamento e moralidade.",
                "LIDO", List.of("Romance", "Clássico"), 5,
                "Perfeito em todos os sentidos. Darcy é eterno! 💕");
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

    private void criarLivroComAvaliacao(Usuario usuario, String titulo, String autor,
                                         String cover, String sinopse, String status,
                                         List<String> categorias, int rating, String comentario) {
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
        livroRepository.save(livro);

        Avaliacao av = new Avaliacao();
        av.setLivroTitulo(titulo);
        av.setLivroAutor(autor);
        av.setUsuarioEmail(usuario.getEmail());
        av.setUsuarioNome(usuario.getNome());
        av.setUsuarioNickname(usuario.getNickname());
        av.setRating(rating);
        av.setComentario(comentario);
        av.setCriadoEm(LocalDateTime.now().minusDays((long)(Math.random() * 30)));
        avaliacaoRepository.save(av);
    }
}
