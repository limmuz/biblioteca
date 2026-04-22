# Projeto Lybre - Biblioteca Pessoal

Aplicacao fullstack para cadastro e gerenciamento de livros, com backend Spring Boot + MongoDB e frontend React (Vite).

## Entrega final

Data de entrega do projeto: 26/05/2026.

## Status rapido (10/04/2026)

Ja existe no repositorio:
- Estrutura backend e frontend criada.
- Autenticacao JWT completa (cadastro, login, rotas protegidas e logout).
- CRUD de livros com busca e delete no backend.
- Integracao frontend com API real (sem mocks no fluxo principal).
- Consulta de CEP no cadastro (ViaCEP) com fallback manual.
- CI inicial configurada (build frontend + testes backend).
- Base de livros atualizada em doc/livros.json com sinopses e metadados.
- Scripts para desenvolvimento local com limpeza de portas (dev:clean).

Pontos que ainda precisam de fechamento:
- Finalizar RTM.md completo com rastreabilidade RF x testes e diagramas UML.
- Refinar responsividade final web/mobile em todas as telas.
- Evoluir cobertura de testes e consolidar evidencias para entrega.
- Integrar fluxo real de redefinicao de senha no backend/frontend.

---

## Atualizacao para o grupo

### O que eu ja conclui
- Auth e sessao completas (cadastro/login + JWT + logout + protected routes).
- CRUD de livros com busca, update e delete.
- Seed de livros atualizado com sinopses e metadados (categorias, editora e publicacao).
- Pipeline CI inicial (build frontend + testes backend).
- Endereco no cadastro com consulta de CEP e fallback manual.
- Ajustes de CORS para localhost em portas dinamicas do frontend.
- Script de inicializacao local com limpeza automatica de portas.

### Enquete de pendencias (escolham 1 parte cada)

Escolham qual opcao cada um vai assumir para fechar rapido:

- [ ] Opcao 1: Ajustes finais de responsividade web/mobile nas telas principais.
- [ ] Opcao 2: Finalizar RTM.md com RF x testes + diagramas UML de sequencia.
- [ ] Opcao 3: Evidencias de qualidade (cobertura >= 80%, link da CI e SonarQube).

Prazo para escolher as opcoes no grupo: 12/04.
Prazo para concluir as pendencias: 12/05.

---

## Cronograma ate 26/05

### 21/04 a 12/05 - Execucao das pendencias pelos colegas
- Colega 1: responsividade web/mobile nas telas principais.
- Colega 2: RTM.md (RF x testes) + diagramas UML de sequencia.
- Colega 3: evidencias de qualidade (cobertura, CI e SonarQube).

### 13/05 a 19/05 - Revisao final em grupo
- Validar entregas dos colegas.
- Corrigir ajustes finais.

### 20/05 a 26/05 - Fechamento da entrega
- Consolidar documentacao final.
- Organizar apresentacao.
- Entregar o projeto em 26/05/2026.

---

## Como rodar localmente

### Pre-requisitos
- Java 17+
- Node.js 18+
- MongoDB local na porta 27017

### Fullstack pelo script raiz (recomendado)
```bash
npm install
npm run dev:clean
```

### Fullstack pelo script raiz (sem limpeza)
```bash
npm run dev
```

### Observacoes importantes
- O comando dev:clean fecha processos presos nas portas 5173/5174/5175/8080 antes de subir o projeto.
- Se importar dados com mongoimport, use o banco Biblioteca (B maiusculo) para manter consistencia local.

### Importar livros no banco local
```bash
"C:\Program Files\MongoDB\Tools\100\bin\mongoimport.exe" --uri "mongodb://localhost:27017/Biblioteca" --collection livros --file "doc/livros.json" --jsonArray --drop
```
