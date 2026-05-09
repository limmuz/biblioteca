/**
 * Detecta e corrige livros com capas duplicadas no banco.
 * Uso: node verificar-capas-duplicadas.mjs <email> <senha>
 */

import http from 'http';

const BASE = 'http://localhost:8080/api';
const [,, email, senha] = process.argv;

if (!email || !senha) {
  console.error('Uso: node verificar-capas-duplicadas.mjs <email> <senha>');
  process.exit(1);
}

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
  const login = await request('POST', '/auth/login', { email, senha });
  if (login.status !== 200) {
    console.error('❌ Login falhou:', login.body?.message || login.status);
    process.exit(1);
  }
  const token = login.body.token;

  const res = await request('GET', '/livros', null, token);
  const livros = res.body || [];

  // Agrupar por URL de capa
  const porCapa = {};
  for (const livro of livros) {
    const capa = livro.cover || '(sem capa)';
    if (!porCapa[capa]) porCapa[capa] = [];
    porCapa[capa].push(livro);
  }

  let duplicatas = 0;
  for (const [capa, grupo] of Object.entries(porCapa)) {
    if (grupo.length > 1) {
      duplicatas++;
      console.log(`\n⚠️  Capa duplicada: ${capa}`);
      for (const l of grupo) {
        console.log(`   [${l.id}] "${l.title}" — status: ${l.status}`);
      }
    }
  }

  if (duplicatas === 0) {
    console.log('\n✅ Nenhuma capa duplicada encontrada!');
  } else {
    console.log(`\n\n${duplicatas} capa(s) duplicada(s) encontrada(s).`);
    console.log('Para corrigir, acesse cada livro na biblioteca e atualize a capa manualmente,');
    console.log('ou informe os IDs acima para que eu crie um script de correção automática.\n');
  }
}

main().catch(console.error);
