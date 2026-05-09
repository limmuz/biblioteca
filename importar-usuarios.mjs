// Script para restaurar usuários diretamente no MongoDB (preserva hash da senha)
// Uso: node importar-usuarios.mjs
// Requer: npm install mongodb  (rode uma vez antes)

import { MongoClient, ObjectId } from 'mongodb';

const URI = 'mongodb+srv://admin:Anappv%402026@biblioteca-cluster.qq5p2t8.mongodb.net/biblioteca_db?retryWrites=true&w=majority';

const usuarios = [
  {
    _id: new ObjectId("69f6b9f3e631579844b0edcc"),
    nome: "ana.pereira.viana",
    email: "ana.pereira.viana@hotmail.com",
    senhaHash: "$2a$10$R7qUcxFKEdwjDDXQQ.tPQeuyiGZ0WMXL8ArevmjTVB2BNqqYjweoa",
    role: "USER",
    cep: "04863120",
    logradouro: "Rua Imbu Natal",
    bairro: "Vila Natal",
    cidade: "São Paulo",
    uf: "SP",
    _class: "com.qs.biblioteca.model.Usuario"
  },
  {
    _id: new ObjectId("69f7c356c752fb5ca7b27563"),
    nome: "paulinhapv22",
    email: "paulinhapv22@gmail.com",
    senhaHash: "$2a$10$fysTa90lPU0LpNUoxFAmuOHSh1KJxGXSIukJMc5/TqorbtD52EzYW",
    role: "USER",
    cep: "04863120",
    logradouro: "Rua Imbu Natal",
    bairro: "Vila Natal",
    cidade: "São Paulo",
    uf: "SP",
    _class: "com.qs.biblioteca.model.Usuario"
  }
];

async function main() {
  const client = new MongoClient(URI);
  try {
    await client.connect();
    console.log('Conectado ao MongoDB Atlas!\n');

    const db = client.db('biblioteca_db');
    const col = db.collection('usuarios');

    for (const usuario of usuarios) {
      const existe = await col.findOne({ email: usuario.email });
      if (existe) {
        console.log(`- ${usuario.email} (já existe, pulando)`);
        continue;
      }
      await col.insertOne(usuario);
      console.log(`✓ ${usuario.email} restaurado`);
    }

    console.log('\nConcluído!');
  } finally {
    await client.close();
  }
}

main().catch(console.error);
