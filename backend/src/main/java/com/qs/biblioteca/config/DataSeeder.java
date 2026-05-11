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

    private static final String SENHA_PADRAO  = "senha123";
    private static final SecureRandom RANDOM  = new SecureRandom();
    private static final int LIVROS_POR_USUARIO = 3;

    private static final String SOFIA_EMAIL   = "sofia@leitor.com";
    private static final String LUCAS_EMAIL   = "lucas@leitor.com";
    private static final String ISABELA_EMAIL = "isabela@leitor.com";

    private static final String CATEGORIA_FC  = "Ficção Científica";

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
        List<Livro> livrosSofia = livroRepository.findByUserEmail(SOFIA_EMAIL);

        if (livrosSofia.size() == LIVROS_POR_USUARIO
                && usuarioRepository.existsByEmail(LUCAS_EMAIL)
                && usuarioRepository.existsByEmail(ISABELA_EMAIL)) {
            return;
        }

        limparDadosSeed(SOFIA_EMAIL);
        limparDadosSeed(LUCAS_EMAIL);
        limparDadosSeed(ISABELA_EMAIL);

        Usuario sofia = usuarioRepository.findByEmail(SOFIA_EMAIL)
                .orElseGet(() -> criarUsuario("Sofia Martins", SOFIA_EMAIL, SENHA_PADRAO,
                        "sofia_reads", "Apaixonada por ficção científica e fantasia ✨"));

        Usuario lucas = usuarioRepository.findByEmail(LUCAS_EMAIL)
                .orElseGet(() -> criarUsuario("Lucas Andrade", LUCAS_EMAIL, SENHA_PADRAO,
                        "lucas_books", "Leitor de thrillers e mistério 🔍"));

        Usuario isabela = usuarioRepository.findByEmail(ISABELA_EMAIL)
                .orElseGet(() -> criarUsuario("Isabela Costa", ISABELA_EMAIL, SENHA_PADRAO,
                        "bela_leitora", "Romance e literatura brasileira são minha vida 🌸"));

        // ── Sofia: Ficção Científica e Fantasia ───────────────────────────────
        Livro guia = criarLivro(sofia,
                "O Guia do Mochileiro das Galáxias", "Douglas Adams",
                gbCover("0345391802"),
                "Arthur Dent descobre que a Terra será demolida para construir uma via expressa "
                        + "interestelar. Com a ajuda de Ford Prefect, embarca em uma jornada absurda pelo cosmos.",
                "LIDO", List.of(CATEGORIA_FC, "Humor"));
        criarAvaliacao(guia, sofia, 5,
                "Absurdo, genial e hilário! A lição mais importante: nunca esqueça a toalha 😂");
        criarAvaliacao(guia, lucas, 4,
                "Sempre quis ler e não me arrependi. O humor britânico é simplesmente impecável!");
        criarAvaliacao(guia, isabela, 5,
                "Uma leitura leve e surpreendente. Douglas Adams era um gênio da comédia literária!");

        Livro fundacao = criarLivro(sofia,
                "Fundação", "Isaac Asimov",
                gbCover("0553293354"),
                "O matemático Hari Seldon prevê o colapso do Império Galáctico e cria a Fundação "
                        + "para preservar o conhecimento humano e encurtar séculos de barbárie.",
                "LIDO", List.of(CATEGORIA_FC));
        criarAvaliacao(fundacao, sofia, 5,
                "Asimov construiu algo grandioso. A psico-história faz pensar no mundo real.");
        criarAvaliacao(fundacao, lucas, 4,
                "Sci-fi clássica e essencial. A construção do universo é simplesmente impressionante.");
        criarAvaliacao(fundacao, isabela, 3,
                "Gostei, mas prefiro narrativas mais centradas em personagens. O conceito é genial.");

        Livro orwell1984 = criarLivro(sofia,
                "1984", "George Orwell",
                gbCover("0451524934"),
                "Em um futuro distópico, Winston Smith vive sob o regime totalitário do Grande Irmão, "
                        + "onde o pensamento independente é crime.",
                "LENDO", List.of(CATEGORIA_FC, "Distopia"));
        criarAvaliacao(orwell1984, lucas, 5,
                "Perturbador e brilhante. Faz pensar no quanto cedemos de liberdade sem perceber.");
        criarAvaliacao(orwell1984, isabela, 4,
                "Leitura pesada, mas absolutamente necessária. Orwell estava décadas à frente.");

        // ── Lucas: Thriller e Mistério ────────────────────────────────────────
        Livro daVinci = criarLivro(lucas,
                "O Código Da Vinci", "Dan Brown",
                gbCover("0307474278"),
                "O professor Robert Langdon é chamado ao Louvre após o assassinato de um curador, "
                        + "desencadeando uma caçada a um segredo milenar da Igreja Católica.",
                "LIDO", List.of("Thriller", "Mistério"));
        criarAvaliacao(daVinci, lucas, 4,
                "Página virada é certeza de suspense. Ótimo para maratonar em um fim de semana!");
        criarAvaliacao(daVinci, sofia, 4,
                "Sabia que iria gostar e fui surpreendida. As reviravoltas são absolutamente viciantes!");
        criarAvaliacao(daVinci, isabela, 3,
                "Divertido, mas personagens um pouco rasos. O plot de ação compensa bastante.");

        Livro garotaExemplar = criarLivro(lucas,
                "Garota Exemplar", "Gillian Flynn",
                gbCover("0307588378"),
                "No dia de seu quinto aniversário de casamento, Amy Dunne desaparece. "
                        + "Seu marido Nick torna-se suspeito número um em um thriller psicológico perturbador.",
                "LIDO", List.of("Thriller", "Suspense"));
        criarAvaliacao(garotaExemplar, lucas, 5,
                "Nunca mais confiei em ninguém depois desse livro. Flynn é absolutamente brilhante!");
        criarAvaliacao(garotaExemplar, sofia, 5,
                "Que viagem! Cada capítulo muda tudo o que você achava que sabia. Obra-prima do suspense.");
        criarAvaliacao(garotaExemplar, isabela, 4,
                "A Amy Dunne é um dos personagens mais fascinantes que já li. Perturbador e genial!");

        criarLivro(lucas,
                "Um Estudo em Vermelho", "Arthur Conan Doyle",
                gbCover("0140439080"),
                "A estreia de Sherlock Holmes e Dr. Watson: um assassinato misterioso em Londres "
                        + "leva o detetive mais famoso da literatura a uma investigação implacável.",
                "QUERO LER", List.of("Mistério", "Clássico"));

        // ── Isabela: Romance e Literatura Brasileira ──────────────────────────
        Livro domCasmurro = criarLivro(isabela,
                "Dom Casmurro", "Machado de Assis",
                gbCover("9780195100723"),
                "Bentinho relembra sua juventude e seu amor por Capitu, "
                        + "mas a dúvida sobre a fidelidade da esposa o consome para sempre.",
                "LIDO", List.of("Romance", "Literatura Brasileira"));
        criarAvaliacao(domCasmurro, isabela, 5,
                "Machado escreveu o ciúme mais elegante da literatura. Capitu culpada ou não? 🤔");
        criarAvaliacao(domCasmurro, sofia, 4,
                "Obra-prima! O narrador não confiável é de enlouquecer. Machado de Assis era um gênio.");
        criarAvaliacao(domCasmurro, lucas, 4,
                "Literatura brasileira no seu melhor. Muito mais envolvente do que eu esperava!");

        Livro horaEstrela = criarLivro(isabela,
                "A Hora da Estrela", "Clarice Lispector",
                gbCover("9780811219006"),
                "Macabéa, uma jovem nordestina ingênua que vive no Rio de Janeiro, "
                        + "é narrada por Rodrigo S. M. em um romance sobre invisibilidade e existência.",
                "LIDO", List.of("Literatura Brasileira"));
        criarAvaliacao(horaEstrela, isabela, 4,
                "Clarice te deixa pensando por dias. Macabéa é absolutamente inesquecível.");
        criarAvaliacao(horaEstrela, sofia, 5,
                "Que escrita! Clarice Lispector é de outro nível. Macabéa ficou gravada no meu coração.");
        criarAvaliacao(horaEstrela, lucas, 3,
                "Muito literário para o meu gosto habitual, mas é inegável o talento de Clarice.");

        Livro cemAnos = criarLivro(isabela,
                "Cem Anos de Solidão", "Gabriel García Márquez",
                gbCover("0060883286"),
                "A saga de sete gerações da família Buendía na fictícia cidade de Macondo, "
                        + "obra-prima do realismo mágico latino-americano.",
                "LENDO", List.of("Romance", "Realismo Mágico"));
        criarAvaliacao(cemAnos, sofia, 5,
                "Ainda estou lendo mas já é o livro mais especial que já segurei nas mãos. Mágico!");
        criarAvaliacao(cemAnos, lucas, 4,
                "Confesso que demorei para entrar na história, mas vale absolutamente cada página.");
    }

    private void limparDadosSeed(String email) {
        livroRepository.deleteAll(livroRepository.findByUserEmail(email));
        avaliacaoRepository.deleteAll(avaliacaoRepository.findByUsuarioEmail(email));
    }

    private static String gbCover(String isbn) {
        return "https://books.google.com/books/content?vid=ISBN:" + isbn
                + "&printsec=frontcover&img=1&zoom=5&source=gbs_api";
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
