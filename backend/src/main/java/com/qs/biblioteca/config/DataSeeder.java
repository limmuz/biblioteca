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
    private static final int LIVROS_POR_USUARIO = 5;

    private static final String SOFIA_EMAIL   = "sofia@leitor.com";
    private static final String LUCAS_EMAIL   = "lucas@leitor.com";
    private static final String ISABELA_EMAIL = "isabela@leitor.com";

    private static final String STATUS_LIDO      = "LIDO";
    private static final String STATUS_LENDO     = "LENDO";
    private static final String STATUS_QUERO_LER = "QUERO LER";

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
                        "sofia_reads", "Apaixonada por quadrinhos, mangás e fantasia ✨"));

        Usuario lucas = usuarioRepository.findByEmail(LUCAS_EMAIL)
                .orElseGet(() -> criarUsuario("Lucas Andrade", LUCAS_EMAIL, SENHA_PADRAO,
                        "lucas_books", "Leitor de mistério, história e graphic novels 🔍"));

        Usuario isabela = usuarioRepository.findByEmail(ISABELA_EMAIL)
                .orElseGet(() -> criarUsuario("Isabela Costa", ISABELA_EMAIL, SENHA_PADRAO,
                        "bela_leitora", "Literatura brasileira e romances são minha vida 🌸"));

        // ── Sofia: Quadrinhos, Mangá e Fantasia ───────────────────────────────
        Livro amari = criarLivro(sofia,
                "Amari e os Irmãos da Noite", "B. B. Alston",
                "/books/book-amari.png",
                "Amari Peters sempre acreditou que seu irmão desaparecido estava vivo. Ao descobrir "
                        + "um mundo secreto de magia e criaturas sobrenaturais, ela precisa provar que pertence "
                        + "a ele — mesmo sendo a única sem poderes mágicos.",
                STATUS_LIDO, List.of("Fantasia", "Aventura", "Juvenil"));
        criarAvaliacao(amari, sofia, 5,
                "Que livro incrível! Amari é uma protagonista corajosa e representativa. Amei cada página!");
        criarAvaliacao(amari, lucas, 4,
                "Não é o meu gênero habitual, mas a construção do mundo mágico é impressionante.");
        criarAvaliacao(amari, isabela, 5,
                "Uma protagonista negra brilhante em um mundo mágico rico. Representatividade importa!");

        Livro persepolis = criarLivro(sofia,
                "Persépolis", "Marjane Satrapi",
                "/books/book-persepolis.png",
                "Autobiografia em quadrinhos de Marjane Satrapi, que cresceu no Irã durante a Revolução "
                        + "Islâmica. Com humor e sensibilidade, retrata a luta de uma jovem por liberdade "
                        + "em meio a um regime opressor.",
                STATUS_LIDO, List.of("Quadrinhos", "Autobiografia", "Político"));
        criarAvaliacao(persepolis, sofia, 5,
                "Uma obra que muda perspectivas. O traço simples e a história profunda são perfeitos juntos.");
        criarAvaliacao(persepolis, lucas, 5,
                "Já tinha ouvido falar muito e finalmente li. A perspectiva histórica é poderosa e única.");
        criarAvaliacao(persepolis, isabela, 4,
                "Emocionante e educativo. Satrapi conta sua história com uma coragem admirável.");

        Livro diferenca = criarLivro(sofia,
                "A Diferença Invisível", "Mademoiselle Caroline & Julie Dachez",
                "/books/book-diferenca.png",
                "Marguerite é uma jovem que nunca se encaixou. Ao descobrir que tem autismo, ela "
                        + "finalmente entende a si mesma. Uma graphic novel delicada sobre neurodivergência, "
                        + "identidade e autoconhecimento.",
                STATUS_LENDO, List.of("Quadrinhos", "Representatividade", "Autoconhecimento"));
        criarAvaliacao(diferenca, sofia, 4,
                "Cada página ressoa de um jeito especial. Me sinto representada de formas que não esperava.");
        criarAvaliacao(diferenca, isabela, 5,
                "Uma leitura necessária para todos. Ajuda a entender e acolher as diferenças de forma linda.");

        Livro caraQue = criarLivro(sofia,
                "O Cara Que Estou a Fim Não É Um Cara?!", "Sumiko Arai",
                "/books/book-cara-que.png",
                "Hinako se apaixona por um colega de classe misterioso chamado Kei — mas descobre "
                        + "que ele é, na verdade, uma garota que se veste como rapaz. Uma comédia romântica "
                        + "manga cheia de mal-entendidos e momentos fofos.",
                STATUS_QUERO_LER, List.of("Mangá", "Romance", "Comédia"));
        criarAvaliacao(caraQue, sofia, 4,
                "Adorei a premissa! Mal posso esperar para ler. As primeiras páginas já me prenderam.");
        criarAvaliacao(caraQue, isabela, 4,
                "A arte é fofa e a história é cheia de potencial. Coloquei na minha lista também!");

        Livro duna = criarLivro(sofia,
                "Duna", "Frank Herbert",
                "/books/book-batalhas.png",
                "Em um deserto planetário chamado Arrakis, Paul Atreides descobre seu destino épico "
                        + "ao lado dos Fremen, em meio a política intergaláctica, religião e ecologia.",
                STATUS_QUERO_LER, List.of("Ficção Científica", "Aventura"));
        criarAvaliacao(duna, lucas, 3,
                "Denso e épico. Exige paciência, mas o mundo construído por Herbert é incomparável.");
        criarAvaliacao(duna, isabela, 4,
                "Comecei por curiosidade e não consegui parar. A complexidade política é fascinante.");

        // ── Lucas: Mistério, História e Graphic Novels ───────────────────────
        Livro sherlock = criarLivro(lucas,
                "Sherlock Holmes - Casos Extraordinários", "Arthur Conan Doyle",
                "/books/book-sherlock.png",
                "Uma coletânea dos melhores casos do detetive mais famoso da literatura. "
                        + "Com sua lógica impecável e observação aguçada, Sherlock Holmes desvenda "
                        + "crimes impossíveis ao lado do fiel Dr. Watson.",
                STATUS_LIDO, List.of("Mistério", "Clássico", "Policial"));
        criarAvaliacao(sherlock, lucas, 5,
                "Conan Doyle criou algo eterno. A lógica de Holmes é hipnotizante do início ao fim!");
        criarAvaliacao(sherlock, sofia, 4,
                "O início de tudo! A elegância do raciocínio de Holmes nunca envelhece. Leitura obrigatória.");
        criarAvaliacao(sherlock, isabela, 4,
                "Clássico absoluto da literatura. Cada caso é uma obra de construção narrativa perfeita.");

        Livro maus = criarLivro(lucas,
                "Maus", "Art Spiegelman",
                "/books/book-maus.png",
                "Vencedor do Prêmio Pulitzer, Maus retrata a história do pai do autor, sobrevivente "
                        + "do Holocausto. Judeus são representados como ratos e nazistas como gatos, "
                        + "em uma obra que redefiniu as possibilidades dos quadrinhos.",
                STATUS_LIDO, List.of("Quadrinhos", "História", "Holocausto"));
        criarAvaliacao(maus, lucas, 5,
                "Obra-prima absoluta. Spiegelman fez algo impossível: tornar o insuportável legível.");
        criarAvaliacao(maus, sofia, 5,
                "Chorei do começo ao fim. A metáfora dos ratos e gatos é perturbadora e perfeita.");
        criarAvaliacao(maus, isabela, 5,
                "Todo mundo deveria ler. A história é devastadora e necessária. Arte que transforma.");

        Livro diarioZlata = criarLivro(lucas,
                "O Diário de Zlata", "Zlata Filipović",
                "/books/book-diario-zlata.png",
                "Aos 11 anos, Zlata Filipović começou a registrar sua vida em Sarajevo durante a "
                        + "guerra da Bósnia. Seu diário, comparado ao de Anne Frank, revela a inocência "
                        + "de uma criança diante dos horrores da guerra.",
                STATUS_LIDO, List.of("Autobiografia", "Guerra", "História"));
        criarAvaliacao(diarioZlata, lucas, 4,
                "Um testemunho poderoso. A visão de uma criança sobre a guerra é de partir o coração.");
        criarAvaliacao(diarioZlata, sofia, 4,
                "A Zlata escreve com uma maturidade impressionante. Um relato que precisa ser lido.");
        criarAvaliacao(diarioZlata, isabela, 5,
                "Emocionante e impactante. A voz de Zlata atravessa o tempo e toca profundamente a alma.");

        Livro rivalidade = criarLivro(lucas,
                "Rivalidade Ardente", "Rachel Reid",
                "/books/book-rivalidade.png",
                "Shane Hollander e Ilya Rozanov são rivais no hóquei profissional — e segredos "
                        + "nos bastidores. Uma história de amor proibido entre dois atletas que o "
                        + "mundo acredita que se odeiam.",
                STATUS_LENDO, List.of("Romance", "Esportes", "LGBTQ+"));
        criarAvaliacao(rivalidade, lucas, 4,
                "Não é o que eu costumo ler, mas a tensão entre os personagens é irresistível. Surpreendente!");
        criarAvaliacao(rivalidade, sofia, 5,
                "Shane e Ilya são minha nova obsessão! A química entre eles é explosiva do começo ao fim.");
        criarAvaliacao(rivalidade, isabela, 5,
                "Rachel Reid escreve romance como ninguém. Chorei, ri e reli várias cenas. Perfeito!");

        Livro garotaTrem = criarLivro(lucas,
                "A Garota no Trem", "Paula Hawkins",
                "/books/book-cabeca-santo.png",
                "Rachel observa diariamente um casal perfeito da janela do trem. Quando a mulher "
                        + "desaparece, Rachel é sugada para uma investigação que coloca sua própria "
                        + "sanidade em dúvida.",
                STATUS_QUERO_LER, List.of("Thriller", "Suspense", "Mistério"));
        criarAvaliacao(garotaTrem, sofia, 4,
                "Tenso do início ao fim! A narrativa não linear é perturbadora da melhor forma.");
        criarAvaliacao(garotaTrem, isabela, 5,
                "Li de uma sentada! Paula Hawkins criou um dos thrillers mais envolventes que já li.");

        // ── Isabela: Literatura Brasileira e Romance ──────────────────────────
        Livro noiva = criarLivro(isabela,
                "A Noiva", "Silas & Villar",
                "/books/book-noiva.png",
                "Uma graphic novel ambientada em uma Europa fantástica onde uma noiva misteriosa "
                        + "e seu noivo sombrio se encontram às vésperas de um casamento que mudará "
                        + "o destino de dois mundos.",
                STATUS_LIDO, List.of("Quadrinhos", "Fantasia", "Romance"));
        criarAvaliacao(noiva, isabela, 5,
                "A arte de Villar é de outro nível. Uma história de amor e mistério absolutamente linda!");
        criarAvaliacao(noiva, sofia, 5,
                "A estética visual é deslumbrante e a história romântica me fisgou completamente. Perfeito!");
        criarAvaliacao(noiva, lucas, 3,
                "Não é o meu estilo, mas reconheço a qualidade artística excepcional da obra.");

        Livro batalhas = criarLivro(isabela,
                "As Batalhas do Castelo", "Domingos Pellegrini",
                "/books/book-batalhas.png",
                "Ricardo é um menino que precisa enfrentar os medos e desafios de crescer. "
                        + "Em batalhas imaginárias no castelo da sua mente, ele aprende que a coragem "
                        + "começa dentro de cada um. Clássico da literatura brasileira infantojuvenil.",
                STATUS_LIDO, List.of("Literatura Brasileira", "Infantojuvenil", "Aventura"));
        criarAvaliacao(batalhas, isabela, 4,
                "Uma leitura da infância que ressignifiquei de adulta. Pellegrini escreve com muita alma.");
        criarAvaliacao(batalhas, sofia, 4,
                "Descobri esse livro pelo perfil da Isabela. Que escrita delicada e poderosa ao mesmo tempo!");
        criarAvaliacao(batalhas, lucas, 3,
                "Uma história simples mas genuína. A prosa brasileira tem uma cadência única e bela.");

        Livro cabecaSanto = criarLivro(isabela,
                "A Cabeça do Santo", "Socorro Acioli",
                "/books/book-cabeca-santo.png",
                "Samuel chega a uma pequena cidade do Nordeste carregando apenas a cabeça de um "
                        + "santo de gesso. O que parecia um simples transporte revela segredos, fé "
                        + "e a alma do sertão brasileiro.",
                STATUS_LENDO, List.of("Literatura Brasileira", "Drama", "Regional"));
        criarAvaliacao(cabecaSanto, isabela, 5,
                "Socorro Acioli capturou a alma do sertão nordestino. Uma escrita mágica e regional.");
        criarAvaliacao(cabecaSanto, sofia, 4,
                "Não conhecia e me surpreendeu demais. O Brasil tem escritoras incríveis esperando ser lidas!");
        criarAvaliacao(cabecaSanto, lucas, 4,
                "A atmosfera do sertão é construída com muita precisão e beleza. Uma leitura marcante.");

        Livro domCasmurro = criarLivro(isabela,
                "Dom Casmurro", "Machado de Assis",
                "/books/book-diferenca.png",
                "Bentinho relembra sua juventude e seu amor por Capitu, mas a dúvida sobre a "
                        + "fidelidade da esposa o consome para sempre. O narrador não confiável "
                        + "mais famoso da literatura brasileira.",
                STATUS_QUERO_LER, List.of("Literatura Brasileira", "Romance", "Clássico"));
        criarAvaliacao(domCasmurro, isabela, 5,
                "Machado escreveu o ciúme mais elegante da literatura. Capitu culpada ou não? 🤔");
        criarAvaliacao(domCasmurro, sofia, 4,
                "O narrador não confiável é de enlouquecer. Machado de Assis era um gênio absoluto.");
        criarAvaliacao(domCasmurro, lucas, 4,
                "Literatura brasileira no seu melhor. Muito mais envolvente do que eu esperava!");

        Livro horaEstrela = criarLivro(isabela,
                "A Hora da Estrela", "Clarice Lispector",
                "/books/book-persepolis.png",
                "Macabéa, uma jovem nordestina ingênua que vive no Rio de Janeiro, é narrada "
                        + "por Rodrigo S. M. em um romance sobre invisibilidade, existência e o que "
                        + "significa ser visto pelo mundo.",
                "RECOMENDADO", List.of("Literatura Brasileira", "Drama"));
        criarAvaliacao(horaEstrela, isabela, 5,
                "Clarice Lispector é de outro planeta. Macabéa ficou gravada em mim para sempre. 💙");
        criarAvaliacao(horaEstrela, sofia, 5,
                "Que escrita! Clarice te obriga a parar e pensar em cada frase. Uma experiência única.");
        criarAvaliacao(horaEstrela, lucas, 3,
                "Muito literário para o meu gosto habitual, mas o talento de Clarice é inegável.");
    }

    private void limparDadosSeed(String email) {
        livroRepository.deleteAll(livroRepository.findByUserEmail(email));
        avaliacaoRepository.deleteAll(avaliacaoRepository.findByUsuarioEmail(email));
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
