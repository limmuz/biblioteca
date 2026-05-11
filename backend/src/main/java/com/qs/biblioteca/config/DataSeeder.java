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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final String SENHA_PADRAO = "senha123";
    private static final SecureRandom RANDOM = new SecureRandom();

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
        // Guarda dupla: retorna se os livros de seed já existem.
        // Garante que usuários existentes não são duplicados,
        // e que livros são recriados se sumirem sem apagar as contas.
        if (!livroRepository.findByUserEmail("sofia@leitor.com").isEmpty()) return;

        Usuario sofia = usuarioRepository.findByEmail("sofia@leitor.com")
                .orElseGet(() -> criarUsuario("Sofia Martins", "sofia@leitor.com", SENHA_PADRAO,
                        "sofia_reads", "Apaixonada por ficção científica e fantasia ✨"));

        Usuario lucas = usuarioRepository.findByEmail("lucas@leitor.com")
                .orElseGet(() -> criarUsuario("Lucas Andrade", "lucas@leitor.com", SENHA_PADRAO,
                        "lucas_books", "Leitor de thrillers e mistério 🔍"));

        Usuario isabela = usuarioRepository.findByEmail("isabela@leitor.com")
                .orElseGet(() -> criarUsuario("Isabela Costa", "isabela@leitor.com", SENHA_PADRAO,
                        "bela_leitora", "Romance e literatura brasileira são minha vida 🌸"));

        // ── Sofia: Ficção Científica e Fantasia ───────────────────────────────
        Livro guia = criarLivro(sofia,
                "O Guia do Mochileiro das Galáxias",
                "Douglas Adams",
                "https://covers.openlibrary.org/b/isbn/0345391802-L.jpg",
                "Arthur Dent descobre que a Terra será demolida para construir uma via expressa "
                        + "interestelar. Com a ajuda de Ford Prefect, embarca em uma jornada absurda pelo cosmos.",
                "LIDO", List.of("Ficção Científica", "Humor"), 224);
        criarAvaliacao(guia, sofia, 5,
                "Absurdo, genial e hilário! A lição mais importante: nunca esqueça a toalha 😂");

        Livro fundacao = criarLivro(sofia,
                "Fundação",
                "Isaac Asimov",
                "https://covers.openlibrary.org/b/isbn/0553293354-L.jpg",
                "O matemático Hari Seldon prevê o colapso do Império Galáctico e cria a Fundação "
                        + "para preservar o conhecimento humano e encurtar séculos de barbárie.",
                "LIDO", List.of("Ficção Científica"), 255);
        criarAvaliacao(fundacao, sofia, 5,
                "Asimov construiu algo grandioso. A psico-história faz pensar no mundo real.");

        criarLivro(sofia,
                "1984",
                "George Orwell",
                "https://covers.openlibrary.org/b/isbn/0451524934-L.jpg",
                "Em um futuro distópico, Winston Smith vive sob o regime totalitário do Grande Irmão, "
                        + "onde o pensamento independente é crime.",
                "LENDO", List.of("Ficção Científica", "Distopia"), 328);

        // ── Lucas: Thriller e Mistério ────────────────────────────────────────
        Livro daVinci = criarLivro(lucas,
                "O Código Da Vinci",
                "Dan Brown",
                "https://covers.openlibrary.org/b/isbn/0307474278-L.jpg",
                "O professor Robert Langdon é chamado ao Louvre após o assassinato de um curador, "
                        + "desencadeando uma caçada a um segredo milenar da Igreja Católica.",
                "LIDO", List.of("Thriller", "Mistério"), 689);
        criarAvaliacao(daVinci, lucas, 4,
                "Página virada é certeza de suspense. Ótimo para maratonar em um fim de semana!");

        Livro garotaExemplar = criarLivro(lucas,
                "Garota Exemplar",
                "Gillian Flynn",
                "https://covers.openlibrary.org/b/isbn/0307588378-L.jpg",
                "No dia de seu quinto aniversário de casamento, Amy Dunne desaparece. "
                        + "Seu marido Nick torna-se suspeito número um em um thriller psicológico perturbador.",
                "LIDO", List.of("Thriller", "Suspense"), 432);
        criarAvaliacao(garotaExemplar, lucas, 5,
                "Nunca mais confiei em ninguém depois desse livro. Flynn é absolutamente brilhante!");

        criarLivro(lucas,
                "Um Estudo em Vermelho",
                "Arthur Conan Doyle",
                "https://covers.openlibrary.org/b/isbn/0140439080-L.jpg",
                "A estreia de Sherlock Holmes e Dr. Watson: um assassinato misterioso em Londres "
                        + "leva o detetive mais famoso da literatura a uma investigação implacável.",
                "QUERO_LER", List.of("Mistério", "Clássico"), 128);

        // ── Isabela: Romance e Literatura Brasileira ──────────────────────────
        Livro domCasmurro = criarLivro(isabela,
                "Dom Casmurro",
                "Machado de Assis",
                "https://covers.openlibrary.org/b/isbn/9780195100723-L.jpg",
                "Bentinho relembra sua juventude e seu amor por Capitu, "
                        + "mas a dúvida sobre a fidelidade da esposa o consome para sempre.",
                "LIDO", List.of("Romance", "Literatura Brasileira"), 256);
        criarAvaliacao(domCasmurro, isabela, 5,
                "Machado escreveu o ciúme mais elegante da literatura. Capitu culpada ou não? 🤔");

        Livro horaEstrela = criarLivro(isabela,
                "A Hora da Estrela",
                "Clarice Lispector",
                "https://covers.openlibrary.org/b/isbn/9780811219006-L.jpg",
                "Macabéa, uma jovem nordestina ingênua que vive no Rio de Janeiro, "
                        + "é narrada por Rodrigo S. M. em um romance sobre invisibilidade e existência.",
                "LIDO", List.of("Literatura Brasileira"), 96);
        criarAvaliacao(horaEstrela, isabela, 4,
                "Clarice te deixa pensando por dias. Macabéa é absolutamente inesquecível.");

        criarLivro(isabela,
                "Cem Anos de Solidão",
                "Gabriel García Márquez",
                "https://covers.openlibrary.org/b/isbn/0060883286-L.jpg",
                "A saga de sete gerações da família Buendía na fictícia cidade de Macondo, "
                        + "obra-prima do realismo mágico latino-americano.",
                "LENDO", List.of("Romance", "Realismo Mágico"), 417);
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
                              String status, List<String> categorias, int paginas) {
        Livro livro = new Livro();
        livro.setUserEmail(usuario.getEmail());
        livro.setTitle(titulo);
        livro.setAuthor(autor);
        livro.setCover(cover);
        livro.setExcerpt(sinopse);
        livro.setStatus(status);
        livro.setCategories(categorias);
        livro.setLanguage("Português");
        livro.setPages(paginas);
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
