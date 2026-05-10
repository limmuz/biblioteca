
function hash(str) {
  let h = 5381;
  for (let i = 0; i < str.length; i++) {
    h = Math.imul(h ^ str.charCodeAt(i), 0x9e3779b9);
    h ^= h >>> 16;
  }
  return Math.abs(h >>> 0);
}

function mkRng(seed) {
  let s = (seed || 1) >>> 0;
  return () => {
    s ^= s << 13; s ^= s >>> 17; s ^= s << 5;
    return (s >>> 0) / 4294967296;
  };
}

function pick(rng, arr) {
  return arr[Math.floor(rng() * arr.length)];
}

function tmpl(tpl, vars) {
  return tpl.replace(/\{(\w+)\}/g, (_, k) => vars[k] ?? k);
}


const NOMES = ['Ana Luz', 'Carlos', 'Helena', 'Rafael', 'Sofia', 'Miguel',
               'Clara', 'Eduardo', 'Beatriz', 'Rodrigo', 'Isabela', 'Tomás',
               'Mariana', 'Felipe', 'Laura', 'Gustavo', 'Cecília', 'Henrique'];

const CENARIOS_GERAL = [
  'uma cidade onde o tempo parece ter parado',
  'um vilarejo escondido entre colinas verdes',
  'uma metrópole que nunca dorme',
  'uma vila costeira de ruas de pedra',
  'um bairro antigo cheio de histórias não contadas',
];
const CENARIOS_ROMANCE = [
  'uma pequena cidade do interior paulista',
  'uma capital europeia no início do outono',
  'uma ilha cercada por mar azul-turquesa',
  'uma fazenda no coração do cerrado brasileiro',
];
const CENARIOS_MISTERIO = [
  'uma mansão isolada no alto da serra',
  'um porto envolto em névoa',
  'um hotel abandonado na periferia da cidade',
  'um convento silencioso de paredes centenárias',
];
const CENARIOS_FICCAO = [
  'uma estação orbital flutuando acima da Terra',
  'uma metrópole subterrânea no ano 2347',
  'um reino paralelo onde a física obedece outras leis',
  'uma colônia em Marte nos primeiros anos de ocupação',
];
const CENARIOS_HIST = [
  'a corte imperial brasileira do século XIX',
  'a Europa devastada pelo pós-guerra',
  'a Lisboa pombalina em reconstrução',
  'o Brasil colonial às vésperas da Independência',
];


const ABERTURA = [
  `A história que {autor} nos oferece em {titulo} começa como um sussurro — discreto, quase imperceptível — mas cresce em cada página até se tornar algo impossível de ignorar. Desde as primeiras linhas, há uma promessa implícita de que o mundo que estamos prestes a habitar é mais complexo do que qualquer aparência sugere.`,

  `{titulo} abre suas portas com a delicadeza característica de {autor}: sem pressa, sem alardes, mas com a precisão de quem sabe exatamente aonde quer chegar. O leitor é convidado a entrar num universo que parece familiar mas que esconde, em cada esquina, algo novo e surpreendente.`,

  `Há livros que chegam silenciosamente e ficam para sempre. {titulo} é um deles. {autor} constrói sua narrativa tijolo a tijolo, com uma paciência artesanal que raramente se encontra na literatura contemporânea, e o resultado é uma obra que fica na memória muito depois da última página.`,

  `O que {autor} fez em {titulo} só pode ser descrito como um ato de coragem literária: escolheu contar uma história que a maioria dos escritores evitaria por saber o quanto ela exige — do narrador, dos personagens e, sobretudo, do leitor. É essa exigência que torna a obra tão recompensadora.`,

  `Às vezes um livro nos encontra no momento exato em que precisamos dele. {titulo} parece ter sido escrito justamente para esses instantes — quando precisamos de palavras que ultrapassem o cotidiano e nos conectem a algo maior do que nós mesmos. {autor} entende esse papel com maestria rara.`,
];

const CENARIO_APRESENTACAO = [
  `{personagem} vivia em {cenario}. Era o tipo de lugar onde todo mundo conhece todo mundo, onde os segredos têm vida curta e as histórias, longa. Havia algo no ar dali — um cheiro de terra molhada e expectativa — que fazia qualquer pessoa sentir que algo estava prestes a acontecer.`,

  `{cenario} — era assim que os moradores mais antigos se referiam ao lugar, com aquela mistura de orgulho e resignação que só o tempo ensina. {personagem} chegou ali por acaso, como se chega a qualquer lugar que depois se torna importante: sem perceber, sem planejar, sem saber que nunca mais seria o mesmo depois disso.`,

  `O primeiro contato de {personagem} com {cenario} foi numa tarde de chuva fina. Havia algo naquele lugar que parecia reconhecê-lo, como se as ruas molhadas, as janelas iluminadas e o cheiro de café passado soubessem quem ele era antes mesmo que ele próprio soubesse o que buscava.`,

  `Quem olhasse {cenario} de fora não veria nada demais — apenas mais um desses lugares que o mapa registra mas que os turistas ignoram. Mas {personagem} sabia, desde que pôs os pés ali, que as aparências mentem com uma elegância particular nesses lugares esquecidos, onde a vida pulsa com uma intensidade inversamente proporcional ao seu tamanho.`,
];

const PERSONAGEM_INTRO = [
  `{personagem} não era o tipo de pessoa que chamava atenção. Andava com aquela discreta segurança de quem não precisa provar nada a ninguém — o que, no fim das contas, é o tipo de presença que mais perturba os que ainda estão tentando se convencer de quem são.`,

  `Havia algo nos olhos de {personagem} que as palavras não alcançavam com facilidade. Uma espécie de conhecimento antigo, daqueles que se acumulam não nos livros mas nos momentos difíceis, nas escolhas erradas que ensinam mais do que qualquer acerto poderia.`,

  `{personagem2} foi quem primeiro notou algo diferente em {personagem}. "Tem gente que carrega o mundo nos ombros sem dobrar", disse uma vez, sem explicar direito o que queria dizer. Mas quem conhecia os dois sabia exatamente do que se tratava.`,

  `A vida de {personagem} poderia ser dividida em dois momentos: antes e depois de {cenario}. Antes, havia uma certa leveza — ou talvez uma certa superficialidade, dependendo do ângulo. Depois, tudo ganhou peso, textura, significado. Como se o mundo tivesse mudado de resolução.`,
];

const CONFLITO = [
  `Foi {personagem2} quem trouxe a notícia — ou talvez o problema, dependendo de como se quer contar. O fato é que, a partir daquele momento, {personagem} soube que a vida como a conhecia havia chegado ao fim. Não de forma dramática, não com estrondos. Apenas deixou de ser o que era.`,

  `A tensão entre {personagem} e {personagem2} crescia há semanas, alimentada por pequenas omissões e meias verdades que se acumulavam como folhas secas no outono. Quando finalmente veio à tona, foi com aquela violência silenciosa das coisas ditas por cima de um jantar, sem tom de voz elevado, com toda a crueldade da racionalidade.`,

  `Havia uma carta. {personagem} a encontrou numa manhã comum, entre contas e propagandas, e desde então nada voltou ao lugar. Letras manuscritas que mudaram o peso do ar, a cor das paredes, o significado de tudo que havia acontecido nos últimos meses.`,

  `O que {personagem} descobriu naquela tarde não era o tipo de coisa que se processa de imediato. Era mais como uma pedra jogada num lago — você vê o impacto, vê as ondas se expandindo, mas a pedra ainda está afundando, ainda chegando ao fundo, e você sabe que vai mudar a paisagem do fundo para sempre.`,
];

const DESENVOLVIMENTO = [
  `Os dias que se seguiram foram de uma estranha normalidade. {personagem} continuou fazendo as mesmas coisas — acordar, trabalhar, comer, dormir — mas com a sensação de que estava representando a própria vida em vez de vivê-la. Às vezes isso basta. Às vezes é a única forma de atravessar.`,

  `{personagem2} apareceu numa quinta-feira sem aviso prévio. Havia algo diferente nele — ou talvez {personagem} é que estava diferente, e tudo o que mudou foi a capacidade de ver o que sempre esteve ali. As conversas que tiveram naqueles dias foram das mais importantes que qualquer um dos dois jamais teve.`,

  `Em {titulo}, {autor} demonstra uma habilidade rara: a de fazer o leitor sentir o tempo passar sem que a narrativa perca um grão de energia. Há um ritmo aqui que lembra a respiração — ora mais acelerado, ora mais lento, mas sempre vivo, sempre presente, sempre indo a algum lugar.`,

  `A questão que {personagem} carregava não era simples, e {autor} não a simplifica. O mérito de {titulo} está precisamente nessa recusa à facilidade: os problemas não têm soluções óbvias, os personagens não têm respostas prontas, e é nessa zona de incerteza que a história encontra sua verdade mais profunda.`,

  `Havia um velho banco de praça onde {personagem} costumava sentar nas tardes de terça-feira. Nada de especial naquele banco, naquela praça, naquelas tardes — exceto que ali tudo parecia mais claro. É esse tipo de detalhe pequeno que {autor} usa com maestria para ancorar a narrativa no real.`,
];

const VIRADA = [
  `Tudo mudou numa noite de chuva. Não era a chuva em si — era o que a chuva fez aparecer: aquela clareza particular dos momentos em que o barulho do mundo se cala e só resta o essencial. {personagem} olhou para {personagem2} e, pela primeira vez, viu sem os filtros que passamos a vida construindo.`,

  `A virada em {titulo} não vem como um trovão, mas como aquela luz de entardecer que muda de ângulo e de repente revela o que estava lá o tempo todo, esperando para ser visto. {autor} entende que as grandes mudanças raramente acontecem de uma vez — elas se acumulam, quietas, até que a soma se torna inevitável.`,

  `{personagem} tomou a decisão numa manhã qualquer, entre o café e a porta. Sem preparação, sem deliberação, com aquela certeza repentina que só existe quando paramos de pensar demais. {personagem2} soube imediatamente, sem que uma palavra precisasse ser dita.`,

  `O momento que divide {titulo} em antes e depois é aquele em que {personagem} escolhe — ou, mais precisamente, em que {personagem} percebe que sempre soube o que queria e que o problema nunca foi a escolha, mas a coragem de fazê-la.`,
];

const REFLEXAO = [
  `Há perguntas que um livro bom não responde — apenas as formula com precisão suficiente para que o leitor as carregue para dentro de si. {titulo} é repleto dessas perguntas. {autor} as semeia ao longo da narrativa com a habilidade de quem sabe que a melhor literatura não explica a vida, mas a ilumina.`,

  `O que fica de {titulo}, além da história em si, é uma certa forma de olhar para o mundo. {autor} nos empresta por algumas horas os olhos de {personagem}, e quando devolvemos esses olhos, algo de diferente permanece no nosso próprio modo de ver.`,

  `{personagem} passou a entender que os grandes momentos da vida raramente se anunciam. Chegam discretos, sem fanfarra, frequentemente disfarçados de coisas menores — uma conversa, um olhar, uma carta encontrada por acaso, uma chuva que chegou na hora certa.`,

  `É curioso como certas histórias, mesmo sendo completamente ficcionais, revelam verdades sobre nossa própria vida que a experiência direta às vezes não consegue mostrar. Essa é a magia que {autor} alcança em {titulo}: fazer o inventado parecer necessário, fazer o imaginado parecer inevitável.`,
];

const CLIMAX = [
  `O encontro final entre {personagem} e {personagem2} aconteceu da única forma possível: sem testemunhas, sem saídas, com toda a verdade disponível e nenhum lugar para escondê-la. {autor} conduz essa cena com a precisão de um relojoeiro: cada palavra no lugar exato, cada silêncio medido com intenção.`,

  `Havia chegado o momento que {titulo} havia preparado desde a primeira página. {personagem} estava ali, no centro do que havia começado como uma pequena inquietação e crescido até consumir tudo ao redor. Não havia mais desvios possíveis — só o caminho que a história havia traçado com cuidado invisível.`,

  `{autor} nos preparou para esse momento sem que percebêssemos. Cada detalhe anteriormente mencionado — cada personagem de passagem, cada cenário descrito com atenção aparentemente excessiva — converge aqui para revelar que nada havia sido acidental. É a marca do grande escritor: fazer o planejado parecer espontâneo.`,
];

const RESOLUCAO = [
  `O que veio depois foi, em muitos sentidos, diferente do que qualquer personagem havia esperado. Não houve grandes discursos nem revelações dramáticas — apenas a vida continuando, modificada, reorganizada em torno do que havia acontecido. {personagem} aprendeu que o depois de qualquer história importante é sempre mais silencioso do que o durante.`,

  `{titulo} termina como começa — discretamente, sem excessos. {autor} não precisa de fogos de artifício para concluir o que construiu com tanta precisão. A última cena tem a qualidade das boas despedidas: deixa espaço para o leitor completar, à sua maneira, o que ficou não dito.`,

  `Meses depois, {personagem} ainda pensava naqueles dias. Não com a nostalgia dolorosa de quem se arrependeu, mas com a gratidão estranha de quem sabe que atravessou algo que o modificou para melhor — mesmo que o processo de modificação não tenha sido fácil, nem rápido, nem indolor.`,

  `Havia uma leveza nova em {personagem} ao final de tudo. Não a leveza da ignorância ou do esquecimento, mas aquela que só existe do outro lado de uma travessia difícil: quando se percebe que o peso que carregávamos era menor do que imaginávamos, ou que estávamos mais fortes do que acreditávamos.`,
];

const REFLEXAO_FINAL = [
  `{titulo} é daqueles livros que continuam sendo escritos dentro do leitor muito depois de fechados. {autor} plantou sementes que germinam devagar, em tempos distintos para cada pessoa, revelando novos significados conforme nossa própria vida avança e nos dá novas ferramentas para entender o que lemos.`,

  `Há uma generosidade essencial em {titulo} que vai além do entretenimento: {autor} partilha, através da ficção, uma visão de mundo que expande a nossa. É isso que a grande literatura faz — não nos diz como a vida é, mas nos mostra que ela pode ser mais do que estamos acostumados a ver.`,

  `Ao fechar {titulo}, fica a certeza de que {autor} entregou algo verdadeiro. Não verdadeiro no sentido dos fatos — a história é ficção, os personagens são inventados — mas verdadeiro no sentido que importa: o das emoções reconhecíveis, dos dilemas universais, das perguntas que nunca saem de moda.`,
];

const PASSAGENS_ADICIONAIS = [
  `{personagem} lembrava com frequência das palavras que {personagem2} havia dito numa tarde distante: "A coragem não é a ausência do medo, mas o que fazemos apesar dele." Eram palavras simples, quase banais, mas que carregavam o peso específico das coisas ditas no momento exato.`,

  `Em {cenario}, o tempo funcionava de forma diferente. As manhãs eram longas e pensativas, as tardes tinham pressa, as noites pertenciam a quem tivesse algo a resolver dentro de si. {personagem} aprendeu a usar cada parte do dia da forma que a cidade ensinava, sem perguntar por quê.`,

  `O que {autor} faz de mais belo em {titulo} é tratar seus personagens com respeito. Não os idealiza, não os diminui — apenas os deixa ser o que são, com suas contradições e suas grandezas, suas mesquinhezas e seus momentos de graça. É uma humanidade que se reconhece na página.`,

  `{personagem2} tinha o hábito de contar histórias que pareciam não ter ponto — histórias que se abriam para outras histórias, como gavetas que guardam outras gavetas. {personagem} demorou para entender que esse era o ensinamento: que nenhuma história tem um ponto verdadeiro, apenas o que escolhemos chamar de fim.`,

  `Havia um café na esquina de {cenario} onde {personagem} passava as manhãs de domingo. O barulho de fundo — colheres, vozes, o rádio ao longe — criava uma espécie de música involuntária que {autor} descreve com aquela precisão sensorial que é uma das marcas de sua escrita.`,

  `A chuva que cai em {titulo} não é apenas clima — é personagem. {autor} a usa com intenção: ela aparece nos momentos de crise, se vai quando as coisas se acomodam, volta quando há mais verdade a ser dita. É esse uso simbólico do simples que distingue a boa literatura da meramente competente.`,

  `"Algumas coisas só se entendem de trás para frente", {personagem} disse certa vez. Era uma das poucas frases que {personagem2} anotou — não num papel, mas na memória, naquele lugar onde guardamos o que não queremos perder mas também não sabemos como usar ainda.`,

  `{titulo} pertence àquela categoria rara de livros que nos fazem sentir menos sozinhos. Não porque ofereçam companhia — são páginas, no fim das contas — mas porque revelam que o que vivemos em silêncio, o que acreditamos ser particular demais para ser dito, é afinal compartilhado por todos que tiveram coragem de sentir de verdade.`,

  `A última vez que {personagem} e {personagem2} se encontraram em {cenario}, o silêncio entre eles tinha uma qualidade diferente: não era mais o silêncio do que não pode ser dito, mas o silêncio do que não precisa ser. Essa diferença — tão pequena e tão enorme ao mesmo tempo — é o coração de {titulo}.`,

  `{autor} escreve sobre o ordinário com reverência. Em {titulo}, um almoço de domingo, uma caminhada no fim do dia, uma ligação que não veio — cada coisa pequena é tratada com a atenção que merece, porque {autor} sabe que é das coisas pequenas que a vida é feita, na maior parte do tempo.`,
];


export function gerarTextoLivro(book) {
  const titulo = book.title || 'esta obra';
  const autor = book.author || 'o autor';
  const cats = (book.categories || []).map(c => c.toLowerCase()).join(' ');

  const isRomance = /romance|amor|sentimental/.test(cats);
  const isMisterio = /mist|suspense|policial|thriller|crime/.test(cats);
  const isFiccao = /fic|sci|fantas|aventura|magic/.test(cats);
  const isHist = /hist|classic|period|colonial|imperial/.test(cats);

  const seed = hash(`${titulo}${autor}`);
  const rng = mkRng(seed);
  const p = (arr) => pick(rng, arr);

  const personagem = p(NOMES);
  const personagem2 = p(NOMES.filter(n => n !== personagem));

  let cenario;
  if (isRomance) cenario = p(CENARIOS_ROMANCE);
  else if (isMisterio) cenario = p(CENARIOS_MISTERIO);
  else if (isFiccao) cenario = p(CENARIOS_FICCAO);
  else if (isHist) cenario = p(CENARIOS_HIST);
  else cenario = p(CENARIOS_GERAL);

  const vars = { titulo, autor, personagem, personagem2, cenario };
  const t = (str) => tmpl(str, vars);

  const paragrafos = [];

  if (book.excerpt && book.excerpt.trim()) {
    const trechos = book.excerpt.trim().split(/\n+/).filter(x => x.trim());
    trechos.forEach(tr => paragrafos.push(tr.trim()));
    paragrafos.push('—');
  }

  paragrafos.push(t(p(ABERTURA)));

  paragrafos.push(t(p(CENARIO_APRESENTACAO)));
  paragrafos.push(t(p(PERSONAGEM_INTRO)));
  paragrafos.push(t(p(PERSONAGEM_INTRO)));

  paragrafos.push('* * *');
  paragrafos.push(t(p(CONFLITO)));
  paragrafos.push(t(p(DESENVOLVIMENTO)));
  paragrafos.push(t(p(DESENVOLVIMENTO)));

  const extras = [...PASSAGENS_ADICIONAIS].sort(() => rng() - 0.5);
  extras.slice(0, 4).forEach(e => paragrafos.push(t(e)));

  paragrafos.push('* * *');
  paragrafos.push(t(p(VIRADA)));
  paragrafos.push(t(p(REFLEXAO)));
  paragrafos.push(t(p(DESENVOLVIMENTO)));

  extras.slice(4, 8).forEach(e => paragrafos.push(t(e)));

  paragrafos.push('* * *');
  paragrafos.push(t(p(CLIMAX)));
  paragrafos.push(t(p(REFLEXAO)));

  extras.slice(8).forEach(e => paragrafos.push(t(e)));

  paragrafos.push('* * *');
  paragrafos.push(t(p(RESOLUCAO)));
  paragrafos.push(t(p(RESOLUCAO)));
  paragrafos.push(t(p(REFLEXAO_FINAL)));
  paragrafos.push(t(p(REFLEXAO_FINAL)));

  return paragrafos;
}

export function paginar(paragrafos, palavrasPorPagina = 300) {
  const paginas = [];
  let atual = [];
  let contagem = 0;

  paragrafos.forEach(par => {
    if (par === '—' || par === '* * *') {
      if (atual.length > 0) {
        atual.push(par);
      }
      return;
    }
    const palavras = par.split(/\s+/).length;
    if (contagem + palavras > palavrasPorPagina && atual.length > 0) {
      paginas.push(atual);
      atual = [];
      contagem = 0;
    }
    atual.push(par);
    contagem += palavras;
  });

  if (atual.length > 0) paginas.push(atual);
  return paginas;
}
