const synopsis = `Pouco antes de morrer, a mãe de Samuel lhe faz um último pedido: que ele vá encontrar a avó e o pai que nunca conheceu. Mesmo contrariado, o rapaz cumpre a promessa e faz a pé o caminho de Juazeiro do Norte até a pequena cidade de Candeia, sofrendo todas as agruras do sol impiedoso do sertão do Ceará.
Ao chegar àquela cidade quase fantasma, ele encontra abrigo num lugar curioso: a cabeça oca e gigantesca de uma estátua inacabada de santo Antônio, que jazia separada do resto do corpo. Mas as estranhezas não param aí: Samuel começa a escutar uma confusão de vozes femininas apenas quando está dentro da cabeça. Assustado, se dá conta de que aquilo são as preces que as mulheres fazem ao santo falando de amor.
Seu primeiro contato na cidade será com Francisco, um rapaz de quem logo fica amigo e que resolve ajudá-lo a explorar comercialmente o seu dom da escuta, promovendo casamentos e outras artimanhas amorosas. Antes parada no tempo, a cidade aos poucos volta à vida, à medida que vai sendo tomada por fiéis de todos os cantos, atraídos pelo poder inaudito de Samuel. Em meio a esse tumulto, ele ainda irá se apaixonar por uma voz misteriosa que se destaca entre as tantas outras que ecoam na cabeça do santo.
Já consagrada por seus livros infantojuvenis, a escritora Socorro Acioli apresenta este seu primeiro romance dirigido ao público adulto, desenvolvido na oficina Como Contar um Conto, promovida por Gabriel García Márquez em Cuba.

"Há muito que não lia com tanto prazer uma história que evocasse, com tão fina elegância , um universo que muito mais que o das crenças rurais nos remete a nossa diversidade interior e a inesgotável necessidade de nos encantarmos." – Mia Couto

"O primeiro romance de Socorro Accioli é uma festa, um divertido exercício de imaginação, partindo de uma ideia que parece roubada aos melhores sonhos de Gabriel Garcia Marquez e desenvolvendo-a até ao final com elegância e mestria." – José Eduardo Agualusa`;

const synopsisExcerpt = `Aya é uma garota do ensino médio que está muito interessada em um "moço" misterioso que trabalha em uma loja de CDs. Porém, esse rapaz misterioso na verdade era Mitsuki, sua colega de classe que tenta viver invisível como o ar. Um amor que se desenvolve com rapidez, a partir de um encontro impossível.`;

export const books = [
  {
    id: 'cabeca-do-santo',
    title: 'A cabeça do santo',
    author: 'Socorro Acioli',
    cover: '/books/book-cabeca-santo.png',
    categories: ['Ficção', 'Realismo mágico', 'Fantasia'],
    language: 'Português',
    publisher: 'Companhia das Letras',
    publishDate: '7 fevereiro 2014',
    pages: 176,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'cara-que-estou',
    title: 'O cara que estou a fim não é um cara?!',
    author: 'Sumiko Arai',
    cover: '/books/book-cara-que.png',
    categories: ['Mangá', 'Romance'],
    language: 'Português',
    publisher: 'JBC',
    publishDate: '2020',
    pages: 180,
    synopsis: synopsisExcerpt,
    excerpt: synopsisExcerpt,
  },
  {
    id: 'rivalidade-ardente',
    title: 'Rivalidade ardente',
    author: 'Rachel Reid',
    cover: '/books/book-rivalidade.png',
    categories: ['Romance', 'Ficção'],
    language: 'Português',
    publisher: 'Faro Editorial',
    publishDate: '2023',
    pages: 380,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'sherlock-holmes',
    title: 'Sherlock Holmes',
    author: 'Arthur Conan Doyle',
    cover: '/books/book-sherlock.png',
    categories: ['Mistério', 'Clássico'],
    language: 'Português',
    publisher: 'FTD',
    publishDate: '2019',
    pages: 320,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'amari',
    title: 'Amari — Irmãos da noite',
    author: 'B. B. Alston',
    cover: '/books/book-amari.png',
    categories: ['Fantasia', 'Jovem Adulto'],
    language: 'Português',
    publisher: 'Intrínseca',
    publishDate: '2022',
    pages: 384,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'diferenca-invisivel',
    title: 'A diferença invisível',
    author: 'Julie Dachez',
    cover: '/books/book-diferenca.png',
    categories: ['Quadrinhos', 'Autobiografia'],
    language: 'Português',
    publisher: 'Nemo',
    publishDate: '2018',
    pages: 184,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'batalhas-do-castelo',
    title: 'As batalhas do castelo',
    author: 'Domingos Pellegrini',
    cover: '/books/book-batalhas.png',
    categories: ['Ficção', 'Aventura'],
    language: 'Português',
    publisher: 'Moderna',
    publishDate: '2021',
    pages: 224,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'diario-de-zlata',
    title: 'Odiário de Zlata',
    author: 'Zlata Filipović',
    cover: '/books/book-diario-zlata.png',
    categories: ['Autobiografia', 'História'],
    language: 'Português',
    publisher: 'Planeta',
    publishDate: '2017',
    pages: 256,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'persepolis',
    title: 'Persépolis',
    author: 'Marjane Satrapi',
    cover: '/books/book-persepolis.png',
    categories: ['Quadrinhos', 'Autobiografia'],
    language: 'Português',
    publisher: 'Conrad',
    publishDate: '2016',
    pages: 280,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'maus',
    title: 'Maus',
    author: 'Art Spiegelman',
    cover: '/books/book-maus.png',
    categories: ['Quadrinhos', 'História'],
    language: 'Português',
    publisher: 'Quadrinhos na Cia.',
    publishDate: '2015',
    pages: 300,
    synopsis,
    excerpt: synopsis,
  },
  {
    id: 'a-noiva',
    title: 'A Noiva',
    author: 'Autor Desconhecido',
    cover: '/books/book-noiva.png',
    categories: ['Romance', 'Ficção'],
    language: 'Português',
    publisher: 'Suma',
    publishDate: '2022',
    pages: 310,
    synopsis,
    excerpt: synopsis,
  },
];

export const recentlyRead = [books[0], books[1], books[2]];
export const readingList = [books[3], books[4], books[5], books[6], books[7], books[8], books[9], books[10]];
export const recommendations = [...books].reverse();

export function getBookById(id) {
  return books.find((b) => b.id === id) || books[0];
}
