# Lybre — Sistema de Biblioteca Pessoal

Sistema de gerenciamento de biblioteca pessoal com autenticação JWT, CRUD de livros, validações de regras de negócio e interface moderna. Desenvolvido com Spring Boot (backend) e React + Vite (frontend), usando **MongoDB Atlas** como banco de dados na nuvem.

> **Nota sobre o repositório:** Este é o fork da Ana Paula (`AnaPaula2024/biblioteca`), criado a partir do repositório original (`limmuz/biblioteca`). O fork foi necessário para configurar a integração com o SonarCloud, que exige que o projeto esteja vinculado a uma conta pessoal do GitHub. O SonarCloud e o GitHub Actions estão configurados neste fork e o link de análise é **público**, podendo ser acessado por qualquer pessoa do grupo.

---

## Como Rodar o Projeto do Zero

Siga os passos abaixo em ordem. Foram testados no Windows, mas funcionam também no Linux e macOS.

### Passo 1 — Instalar o Git

O Git é necessário para clonar o repositório.

1. Acesse https://git-scm.com/downloads e baixe o instalador para o seu sistema
2. Execute o instalador com as opções padrão
3. Verifique a instalação abrindo o terminal (PowerShell ou CMD) e digitando:
   ```
   git --version
   ```
   Deve aparecer algo como `git version 2.x.x`

### Passo 2 — Instalar o Java 17

O backend exige o Java JDK 17 (versões mais novas também funcionam, mas 17 é a versão usada no projeto).

1. Acesse https://adoptium.net/ e baixe o **Temurin 17 (LTS)**
2. Execute o instalador com as opções padrão
3. Verifique a instalação:
   ```
   java -version
   ```
   Deve aparecer `openjdk version "17.x.x"`

> O Maven (ferramenta de build do backend) já vem embutido no projeto via `mvnw` / `mvnw.cmd` — **não precisa instalar o Maven separadamente**.

### Passo 3 — Instalar o Node.js

O frontend exige o Node.js v18 ou superior.

1. Acesse https://nodejs.org/ e baixe a versão **LTS** (recomendada)
2. Execute o instalador com as opções padrão
3. Verifique a instalação:
   ```
   node -v
   npm -v
   ```
   Deve aparecer `v20.x.x` (ou superior) para o Node e `10.x.x` para o npm

### Passo 4 — Clonar o Repositório

```bash
git clone https://github.com/AnaPaula2024/biblioteca.git
cd biblioteca
```

### Passo 5 — Configurar o Banco de Dados (MongoDB Atlas)

O projeto usa o **MongoDB Atlas** (banco na nuvem). **Não é necessário instalar MongoDB localmente.**

1. Copie o arquivo de exemplo de configuração:

   **Windows (PowerShell):**
   ```powershell
   Copy-Item backend\src\main\resources\application.properties.example backend\src\main\resources\application.properties
   ```

   **Linux / macOS:**
   ```bash
   cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
   ```

2. Abra o arquivo `backend/src/main/resources/application.properties` em um editor de texto e preencha com as credenciais reais:

   ```properties
   spring.data.mongodb.uri=mongodb+srv://USUARIO:SENHA@biblioteca-cluster.xxxxx.mongodb.net/biblioteca_db?retryWrites=true&w=majority
   jwt.secret=CHAVE_SECRETA_AQUI
   jwt.expiration-ms=86400000
   cloudinary.cloud-name=seu_cloud_name
   cloudinary.api-key=sua_api_key
   cloudinary.api-secret=seu_api_secret
   ```

   > As credenciais de acesso (usuário, senha, chave JWT e Cloudinary) não ficam no repositório por segurança. Entre em contato com a Ana Paula para receber as informações de conexão.

   > O arquivo `application.properties` está no `.gitignore` e **nunca deve ser commitado** — ele contém credenciais reais.

### Passo 6 — Rodar o Backend

**Windows:**
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
cd backend
./mvnw spring-boot:run
```

Aguarde até aparecer a mensagem `Started BibliotecaApplication`. O backend estará rodando em: **http://localhost:8080**

> Se aparecer erro de conexão com o banco, verifique se o `application.properties` foi configurado corretamente no Passo 5.

### Passo 7 — Rodar o Frontend

Abra um **novo terminal** (sem fechar o terminal do backend) e execute:

```bash
cd frontend
npm install
npm run dev
```

O frontend estará acessível em: **http://localhost:5173**

---

## Rodar os Testes Automatizados

> Requer **Docker instalado e rodando** — os testes usam Testcontainers para subir um MongoDB real e efêmero durante a execução.
>
> - Windows: [Docker Desktop](https://www.docker.com/products/docker-desktop/) — deve estar aberto antes de rodar os testes
> - Linux: Docker Engine — verifique com `docker ps`
>
> **Para rodar a aplicação em si, Docker não é necessário** — o banco está no MongoDB Atlas.

**Windows:**
```powershell
cd backend
.\mvnw.cmd clean verify
```

**Linux / macOS:**
```bash
cd backend
./mvnw clean verify
```

Se todos os testes passarem, você verá `BUILD SUCCESS`. O relatório de cobertura JaCoCo é gerado em `backend/target/site/jacoco/index.html`.

---

## O que o projeto atende (requisitos)

| Requisito | Status |
|---|---|
| CRUD de livros com MongoDB | Implementado |
| Cadastro e autenticação de usuários | Implementado com JWT |
| Gerenciamento de sessão no frontend | Token salvo no localStorage |
| Spring Boot + Arquitetura MVC | Implementado |
| Testcontainers (sem Mocks) | Todos os testes usam MongoDB real |
| Testes unitários parametrizados | `LivroValidatorParamTest.java` |
| Testes de integração | `LivroServiceIntegrationTest.java` |
| Testes E2E / Caixa Preta | `AuthE2ETest.java` + `LivroE2ETest.java` + `AvaliacaoE2ETest.java` + `UsuarioE2ETest.java` + `ImagemE2ETest.java` + `NotificacaoE2ETest.java` |
| Integração com API externa real | ViaCEP (busca de CEP no cadastro) — teste real via `ViaCepIntegrationTest.java` |
| Upload de imagem via Cloudinary | Avatar enviado para Cloudinary e URL armazenada no banco; endpoint `/api/imagens/upload` |
| RF-20 (Upload de avatar) | Implementado com `ImagemController` + `CloudinaryService` |
| RF-21 (Notificações de atividade) | Implementado com `NotificacaoController` + `NotificacaoService`; seguidores recebem notificação quando um leitor avalia um livro; contagem de não lidas e marcar como lidas |
| RF-22 (Exclusão permanente de livro) | Criador pode excluir livro da biblioteca de todos os leitores; cascata: avaliações excluídas e notificação enviada a cada afetado |
| RF-23 (Recomendações da comunidade) | Endpoint `/livros/nao-tenho` retorna livros que outros leitores possuem e o usuário ainda não tem; usado nas recomendações da HomePage e DetalhesLivroPage |
| RF-24 (Excluir notificação individual) | Endpoint `DELETE /api/notificacoes/{id}` permite excluir uma notificação específica; retorna 204 |
| RF-25 (Restrição de criador + limpeza de órfãos) | Botão "Tirar da biblioteca" exibido apenas para o criador do livro; ao excluir o último exemplar de um título, avaliações órfãs são removidas automaticamente |
| Cobertura >= 80% (JaCoCo) | 88.3% no SonarCloud — check JaCoCo aprovado |
| SonarCloud configurado | Rodando — https://sonarcloud.io/project/overview?id=AnaPaula2024_biblioteca |
| CI com GitHub Actions | Pipeline configurado |

## Funcionalidades do sistema

| Funcionalidade | Descrição |
|---|---|
| Biblioteca pessoal | Adicionar, editar, excluir e organizar livros por status (Lido, Lendo, Quero Ler) |
| Avaliações com estrelas | Avaliar livros de 1 a 5 estrelas com comentário; ver média de todos os leitores |
| Curtir e responder comentários | Curtir (toggle) a avaliação de outro leitor e responder com texto; excluir própria resposta |
| Meus Favoritos | Seção automática com livros avaliados com 4 ou 5 estrelas |
| Personalização de perfil | Foto de perfil enviada ao Cloudinary (URL armazenada), plano de fundo, fonte da página, bio e meta de leitura |
| Perfil público/privado | Toggle visível no perfil; perfil privado retorna 403 para outros usuários |
| Conheça outros leitores | Carousel de perfis públicos com cards com fundo, avatar, bio e botões de ação |
| Perfis adicionados | Seção para acompanhar leitores favoritados (persistido em localStorage) |
| Perfil público detalhado | Ver livros, estatísticas e fundo de outro leitor; clicar em livro abre detalhes; adicionar livros à própria biblioteca a partir do perfil ou da página de detalhes |
| Ver perfil por ID | Usuários sem nickname têm perfil acessível via `/leitor/id/{id}` |
| Busca de leitores | Buscar por nome ou @nickname na seção social do perfil |
| Estatísticas de leitura | Contadores de lidos, lendo e quero ler com mosaico visual |
| Navegação por seção | Setas prev/next no detalhe do livro percorrem apenas os livros da seção de origem |
| Propagação de metadados | Edição de capa/sinopse/editora por um usuário atualiza automaticamente as cópias de outros usuários com o mesmo livro |
| Exclusão permanente | Criador do livro pode excluir de todas as bibliotecas ao mesmo tempo; avaliações são removidas em cascata e cada leitor afetado recebe notificação |
| Notificações de atividade | Seguidores recebem notificação quando um leitor avalia um livro; contagem de não lidas exibida no ícone do sino; marcar todas como lidas; excluir notificação individual |
| Recomendações da comunidade | HomePage e DetalhesLivroPage sugerem livros que outros leitores possuem e o usuário ainda não tem (endpoint `/livros/nao-tenho`), com fallback para `/livros/descobrir` |
| Restrição de criador | Botões "Tirar da biblioteca" e "Excluir permanente" aparecem apenas para o leitor que cadastrou o livro; outros leitores não veem as opções de exclusão |
| Limpeza de avaliações órfãs | Ao excluir o último exemplar de um título+autor da plataforma, as avaliações desse livro são removidas automaticamente do banco |
| Repositório no GitHub | https://github.com/AnaPaula2024/biblioteca |

---

## Testes Disponíveis

| Arquivo | Localização | O que testa |
|---|---|---|
| `AuthE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-01 (Cadastro) e RF-02 (Login) |
| `LivroE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-03 a RF-08 + RF-23 (CRUD de livros, sessão, nao-tenho e descobrir) |
| `AvaliacaoE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-10 a RF-12 (criar, listar, excluir avaliações; curtir/descurtir e responder comentários) |
| `UsuarioE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-13 a RF-16 (perfil, personalização, perfil público, busca e listagem de leitores — inclui exclusão de perfis privados) |
| `ImagemE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-20 (upload de avatar: sem token → 401; Cloudinary não configurado no perfil de teste → 503) |
| `NotificacaoE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-21 + RF-24 (notificações: listar, contagem, marcar como lidas, excluir por ID; 401 sem token em cada endpoint) |
| `LivroServiceIntegrationTest.java` | `backend/src/test/java/com/qs/biblioteca/integration/` | RF-04 a RF-07 + RF-23 (integração com MongoDB, incluindo naoTenho e enriquecimento de metadados) |
| `ViaCepIntegrationTest.java` | `backend/src/test/java/com/qs/biblioteca/integration/` | RF-09 (integração real com API ViaCEP — sem mocks) |
| `LivroValidatorParamTest.java` | `backend/src/test/java/com/qs/biblioteca/unit/` | Validações de negócio (unitário parametrizado — caixa branca) |

---

## Banco de Dados — Estrutura

| Coleção | Campos principais |
|---|---|
| `livros` | `title`, `author`, `cover`, `excerpt`, `status`, `language`, `pages`, `categories`, `publisher`, `publishedDate`, `userEmail` |
| `usuarios` | `nome`, `email`, `senhaHash`, `role`, `cep`, `logradouro`, `bairro`, `cidade`, `uf`, `enderecos`, `telefones`, `redesSociais`, `avatarBase64` (URL Cloudinary), `bgBase64`, `bio`, `nickname`, `perfilPublico`, `leitoresSeguidos` (lista de IDs), `metaLeitura` |
| `avaliacoes` | `livroTitulo`, `livroAutor`, `livroId`, `livroCover`, `usuarioEmail`, `usuarioNome`, `usuarioNickname`, `avatarBase64` (URL Cloudinary), `rating`, `comentario`, `criadoEm`, `curtidas` (lista de emails), `respostas` (lista de objetos com `id`, `usuarioEmail`, `usuarioNome`, `usuarioNickname`, `avatarBase64`, `texto`, `criadoEm`, `minha`) |
| `notificacoes` | `usuarioEmail`, `tipo` (ex: `AVALIACAO_CRIADA`, `LIVRO_EXCLUIDO_PERMANENTE`), `livroTitulo`, `livroAutor`, `remetente`, `atorEmail`, `detalhe`, `lida` (boolean), `criadaEm` |

---

## Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Backend |
| Spring Boot | 3.4.5 | Framework backend |
| MongoDB Atlas | 7.0 | Banco de dados na nuvem |
| React + Vite | 18+ | Frontend |
| Node.js | 20+ | Runtime do frontend |
| JUnit 5 | - | Testes automatizados |
| Testcontainers | 1.21.3 | MongoDB real nos testes |
| JaCoCo | 0.8.12 | Relatório de cobertura de testes |
| ViaCEP | API pública | API externa real de consulta de CEP (integração real nos testes) |
| Cloudinary | SDK v1.39.0 | Hospedagem de imagens (avatar do perfil) |
| SonarCloud | - | Análise de qualidade de código |
| GitHub Actions | - | CI/CD automatizado |

---

## CI/CD — GitHub Actions

O pipeline roda automaticamente a cada push:

- **Configuração:** `.github/workflows/ci.yml`
- **O que executa:** compilação, testes, relatório JaCoCo e análise SonarCloud
- **Histórico:** https://github.com/AnaPaula2024/biblioteca/actions

---

*Última atualização: 15/05/2026*
