/**
 * Importa os livros das capas da pasta /public/books como RECOMENDADO.
 * Uso: node importar-recomendacoes.mjs <email> <senha>
 * Exemplo: node importar-recomendacoes.mjs paulinhapv22@gmail.com "Anappv@2026"
 */

import https from 'https';
import http from 'http';

const BASE = 'http://localhost:8080/api';
const [,, email, senha] = process.argv;

if (!email || !senha) {
  console.error('Uso: node importar-recomendacoes.mjs <email> <senha>');
  process.exit(1);
}

const LIVROS = [
  {
    title: 'A Cabeça do Santo',
    author: 'Socorro Acioli',
    cover: '/books/book-cabeca-santo.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 144,
    categories: ['Romance', 'Literatura Brasileira'],
    publisher: 'Companhia das Letras',
    publishedDate: '2014',
    excerpt: 'Uma estátua de santo esquecida numa fazenda do sertão nordestino guarda um segredo: ela consegue ouvir os pensamentos e desejos das pessoas. Quando um fotógrafo vindo de São Paulo chega à região, a vida de toda a comunidade muda para sempre.',
  },
  {
    title: 'Persépolis',
    author: 'Marjane Satrapi',
    cover: '/books/book-persepolis.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 153,
    categories: ['Graphic Novel', 'Autobiografia', 'História'],
    publisher: 'Companhia das Letras',
    publishedDate: '2000',
    excerpt: 'Persépolis é a história da infância e adolescência de Marjane Satrapi no Irã durante a Revolução Islâmica. Com traços em preto e branco, a autora narra com humor e sensibilidade um período turbulento da história iraniana pelos olhos de uma menina curiosa e rebelde.',
  },
  {
    title: 'Maus',
    author: 'Art Spiegelman',
    cover: '/books/book-maus.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 296,
    categories: ['Graphic Novel', 'História', 'Segunda Guerra'],
    publisher: 'Companhia das Letras',
    publishedDate: '1991',
    excerpt: 'Art Spiegelman narra a sobrevivência de seu pai ao Holocausto, representando os judeus como ratos e os nazistas como gatos. Uma obra que ganhou o Prêmio Pulitzer e redefiniu as possibilidades da literatura em quadrinhos.',
  },
  {
    title: 'O Diário de Zlata',
    author: 'Zlata Filipović',
    cover: '/books/book-diario-zlata.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 197,
    categories: ['Diário', 'Autobiografia', 'História'],
    publisher: 'Planeta',
    publishedDate: '1994',
    excerpt: 'Durante o cerco de Sarajevo na guerra da Bósnia, Zlata Filipović, então com 11 anos, começou a registrar o cotidiano assustador em seu diário. Comparado ao Diário de Anne Frank, o livro mostra a guerra pelos olhos de uma criança que só quer voltar a brincar e ir à escola.',
  },
  {
    title: 'Amari e os Irmãos da Noite',
    author: 'B.B. Alston',
    cover: '/books/book-amari.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 432,
    categories: ['Fantasia', 'Aventura', 'Jovem Adulto'],
    publisher: 'Seguinte',
    publishedDate: '2021',
    excerpt: 'Amari Peters sempre acreditou no impossível — até descobrir que o irmão desaparecido deixou para ela um case cheio de segredos e uma vaga no Bureau Sobrenatural. Ali, ela descobre que magia existe e que seu lugar pode ser maior do que jamais imaginou.',
  },
  {
    title: 'A Diferença Invisível',
    author: 'Julie Dachez',
    cover: '/books/book-diferenca.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 184,
    categories: ['Graphic Novel', 'Não Ficção', 'Saúde Mental'],
    publisher: 'Nemo',
    publishedDate: '2016',
    excerpt: 'Marguerite tem 27 anos e sente que nunca se encaixou em nada. Depois de anos buscando respostas, recebe o diagnóstico de autismo. Uma história tocante sobre identidade, pertencimento e autoconhecimento em quadrinhos.',
  },
  {
    title: 'As Batalhas do Castelo',
    author: 'Dominique Pellagiol',
    cover: '/books/book-batalhas.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 128,
    categories: ['Infantojuvenil', 'Aventura', 'Literatura Brasileira'],
    publisher: 'Somos',
    publishedDate: '2019',
    excerpt: 'Um grupo de crianças descobre que o casarão abandonado no fim da rua esconde segredos que ninguém esperava. Com coragem e muita imaginação, elas vão enfrentar desafios que parecem maiores do que qualquer batalha.',
  },
  {
    title: 'Sherlock Holmes: Aventuras Completas',
    author: 'Arthur Conan Doyle',
    cover: '/books/book-sherlock.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 307,
    categories: ['Mistério', 'Clássico', 'Aventura'],
    publisher: 'FTD',
    publishedDate: '2016',
    excerpt: 'O detetive mais famoso da literatura mundial reúne neste volume suas aventuras mais empolgantes. Com a ajuda fiel de Dr. Watson, Sherlock Holmes usa lógica e raciocínio dedutivo extraordinário para solucionar crimes que deixam Scotland Yard perplexa.',
  },
  {
    title: 'A Noiva',
    author: 'Bianca Marais',
    cover: '/books/book-noiva.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 256,
    categories: ['Romance', 'Drama', 'Ficção Histórica'],
    publisher: 'Record',
    publishedDate: '2018',
    excerpt: 'Uma jovem noiva chega a uma nova cidade carregando segredos que o passado não deixa enterrar. Entre véus e promessas, ela vai descobrir que o amor verdadeiro exige coragem — e que nem todo casamento começa com uma festa.',
  },
  {
    title: 'O Cara que o Professor Ama Odiar',
    author: 'Gordon Korman',
    cover: '/books/book-cara-que.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 229,
    categories: ['Infantojuvenil', 'Humor', 'Escola'],
    publisher: 'Intrínseca',
    publishedDate: '2015',
    excerpt: 'Quando um garoto problemático chega numa nova escola, o professor mais rígido do colégio decide que vai fazê-lo seguir as regras de uma vez por todas. O resultado é uma batalha hilária de vontades que vai fazer qualquer leitor se reconhecer — e rir muito.',
  },
  {
    title: 'A Rivalidade',
    author: 'Manu Digilio',
    cover: '/books/book-rivalidade.png',
    status: 'RECOMENDADO',
    language: 'Português',
    pages: 256,
    categories: ['Romance', 'Jovem Adulto', 'Esporte'],
    publisher: 'Seguinte',
    publishedDate: '2020',
    excerpt: 'Duas atletas que sempre foram rivais dentro de quadra descobrem que fora dela as coisas são mais complicadas. Entre treinos, campeonatos e sentimentos confusos, elas terão que decidir o que realmente importa — e o que vale a pena lutar.',
  },
];

function request(method, path, body, token) {
  return new Promise((resolve, reject) => {
    const url = new URL(BASE + path);
    const options = {
      hostname: url.hostname,
      port: url.port || 80,
      path: url.pathname,
      method,
      headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    };
    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => data += chunk);
      res.on('end', () => resolve({ status: res.statusCode, body: data ? JSON.parse(data) : null }));
    });
    req.on('error', reject);
    if (body) req.write(JSON.stringify(body));
    req.end();
  });
}

async function main() {
  console.log(`\n📚 Importador de Recomendações`);
  console.log(`📧 Usuário: ${email}\n`);

  // Login
  const login = await request('POST', '/auth/login', { email, senha });
  if (login.status !== 200) {
    console.error('❌ Login falhou:', login.body?.message || login.status);
    process.exit(1);
  }
  const token = login.body.token;
  console.log('✅ Login OK\n');

  // Buscar livros existentes
  const existing = await request('GET', '/livros', null, token);
  const existingTitles = new Set(
    (existing.body || []).map(b => b.title?.toLowerCase().trim())
  );

  let adicionados = 0;
  let pulados = 0;

  for (const livro of LIVROS) {
    const key = livro.title.toLowerCase().trim();
    if (existingTitles.has(key)) {
      console.log(`⏭️  Já existe: ${livro.title}`);
      pulados++;
      continue;
    }

    const res = await request('POST', '/livros', livro, token);
    if (res.status === 201 || res.status === 200) {
      console.log(`✅ Adicionado: ${livro.title}`);
      adicionados++;
    } else {
      console.log(`⚠️  Erro em "${livro.title}": ${res.status} — ${res.body?.message || ''}`);
    }
  }

  console.log(`\n🎉 Concluído! ${adicionados} adicionados, ${pulados} já existiam.`);
  console.log(`💡 Os livros aparecerão na seção "Recomendações" da home.\n`);
}

main().catch(console.error);
