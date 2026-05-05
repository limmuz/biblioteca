# 📚 Lybre — Sistema de Biblioteca Pessoal

Sistema de gerenciamento de biblioteca pessoal com autenticação JWT, CRUD de livros, validações de regras de negócio e interface moderna. Desenvolvido com Spring Boot (backend) e React + Vite (frontend), usando **MongoDB Atlas** como banco de dados na nuvem.

> **Nota sobre o repositório:** Este é o fork da Ana Paula (`AnaPaula2024/biblioteca`), criado a partir do repositório original (`limmuz/biblioteca`). O fork foi necessário para configurar a integração com o SonarCloud, que exige que o projeto esteja vinculado a uma conta pessoal do GitHub — não é possível configurar o SonarCloud diretamente no repositório de outra pessoa sem ser administrador. O SonarCloud e o GitHub Actions estão configurados neste fork e o link de análise é **público**, podendo ser acessado por qualquer pessoa do grupo.

---

## 👥 Divisão do Trabalho no Grupo

### Ana Paula — Backend, Infraestrutura e Frontend parcial
- Implementou toda a autenticação JWT (registro, login, proteção de rotas)
- Desenvolveu o CRUD completo de livros e usuários no backend
- Configurou o Testcontainers (MongoDB real nos testes, sem Mocks)
- Configurou o SonarCloud e o pipeline CI com GitHub Actions
- Integrou a API ViaCEP no cadastro de usuário (busca automática de endereço por CEP)
- Configurou o MongoDB Atlas como banco de dados na nuvem
- Implementou validação de livro duplicado (retorna erro 409)
- Corrigiu layout da tela inicial (mensagem de lista vazia centralizada)
- Corrigiu layout da página de detalhes do livro
- Desenvolveu a página de perfil do usuário (PerfilPage) com endereços, telefones e redes sociais
- Desenvolveu a página de edição de livro (EditarLivroPage)
- Adicionou carrossel de últimas leituras e recomendações na tela inicial
- Refatorou a `CadastroPage.jsx` para adicionar a integração com a API ViaCEP (em cima do trabalho da Nicole)
- Ajustou o `LivroValidator.java` e o `LivroService.java` para adicionar a validação de livro duplicado (em cima do trabalho do Miguel)

### Miguel — Testes e Qualidade de Código
- Criou todos os testes automatizados: E2E (`AuthE2ETest.java`, `LivroE2ETest.java`), integração (`LivroServiceIntegrationTest.java`) e unitários parametrizados (`LivroValidatorParamTest.java`)
- Configurou o Testcontainers (`TestcontainersConfiguration.java`) e o `application-test.properties`
- Criou o `LivroValidator.java` com as regras de validação de negócio
- Configurou o JaCoCo no `pom.xml` e alcançou 96% de cobertura de código
- Ajustou o `LivroValidator` para corrigir os testes E2E

### Nicole — Frontend
- Atualizou o header com novos ícones e ajustes de estilo (`AppHeader.jsx`, `AppHeader.module.css`)
- Ajustou o layout da página de cadastro (`CadastroPage.jsx`, `LoginPage.module.css`)
- Atualizou o `.gitignore` e ajustes no `RegisterRequest.java`
- Contribuiu com ajustes na `HomePage.jsx`

---

## ✅ O que o projeto atende (requisitos do professor)

| Requisito | Status |
|---|---|
| CRUD de livros com MongoDB | ✅ Implementado |
| Cadastro e autenticação de usuários | ✅ Implementado com JWT |
| Gerenciamento de sessão no frontend | ✅ Token salvo no localStorage |
| Spring Boot + Arquitetura MVC | ✅ Implementado |
| Testcontainers (sem Mocks) | ✅ Todos os testes usam MongoDB real |
| Testes unitários parametrizados | ✅ `LivroValidatorParamTest.java` |
| Testes de integração | ✅ `LivroServiceIntegrationTest.java` |
| Testes E2E / Caixa Preta | ✅ `AuthE2ETest.java` + `LivroE2ETest.java` |
| VCR para API externa | ✅ ViaCEP (busca de CEP no cadastro) com WireMock |
| Cobertura ≥ 80% (JaCoCo) | ✅ 96% alcançado |
| SonarCloud configurado | ✅ Rodando — ver https://sonarcloud.io/project/overview?id=AnaPaula2024_biblioteca |
| CI com GitHub Actions | ✅ Pipeline configurado |
| Repositório no GitHub | ✅ https://github.com/AnaPaula2024/biblioteca |
| README detalhado | ✅ Este arquivo |

---

## ⚠️ O que ainda falta completar (para o grupo)

### Para o integrante responsável pela documentação:

1. **Criar os 8 Diagramas UML de Sequência** (OBRIGATÓRIO pelo professor)
   - Um diagrama para cada requisito funcional (RF-01 a RF-08)
   - Mostrar o fluxo: Usuário → Frontend → Backend → MongoDB → Resposta
   - Ferramentas sugeridas: [PlantUML](https://www.plantuml.com/plantuml/uml/) ou [Draw.io](https://app.diagrams.net/)
   - Salvar na pasta `doc/diagramas/` e colocar o link no `RTM.md`

2. **Preencher o RTM.md** com os arquivos de teste reais e atualizar os status
   - Os testes já existem — só precisa associar cada um ao seu requisito funcional
   - Ver seção "Como preencher o RTM.md" abaixo

3. **Adicionar evidências** (prints) no RTM.md:
   - Print do relatório JaCoCo mostrando 96% de cobertura
   - Print do SonarCloud
   - Print do GitHub Actions verde

---

## 📋 Como preencher o RTM.md

O arquivo `RTM.md` já está no repositório com a estrutura pronta. Os testes já existem — só falta associar cada teste ao seu requisito e criar os diagramas UML.

### Testes disponíveis no projeto:

| Arquivo | Localização | O que testa |
|---|---|---|
| `AuthE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-01 (Cadastro) e RF-02 (Login) |
| `LivroE2ETest.java` | `backend/src/test/java/com/qs/biblioteca/e2e/` | RF-03 a RF-08 (CRUD de livros + sessão) |
| `LivroServiceIntegrationTest.java` | `backend/src/test/java/com/qs/biblioteca/integration/` | RF-04 a RF-07 (integração com MongoDB) |
| `LivroValidatorParamTest.java` | `backend/src/test/java/com/qs/biblioteca/unit/` | Validações de negócio (unitário parametrizado) |

### Como rodar os testes e gerar o relatório JaCoCo (para evidência no RTM):

> ⚠️ **Requer Docker rodando** — os testes usam Testcontainers para subir um MongoDB real e efêmero.

**Windows:**
```bash
cd backend
mvnw.cmd clean test
```

**Linux / macOS:**
```bash
cd backend
./mvnw clean test
```

Após `BUILD SUCCESS`, o relatório HTML é gerado em `backend/target/site/jacoco/index.html`. Abra no navegador, tire um print e adicione como evidência no RTM.md.

### Exemplo de diagrama UML para RF-01 (Cadastro de Usuário):
```
Usuário → CadastroPage → api.post('/api/auth/register')
  → AuthController.register()
  → UsuarioService.registrar()
  → UsuarioValidator.validarRegistro()
  → UsuarioRepository.save()
  → MongoDB Atlas
  ← Retorna token JWT
  ← Salva token no localStorage
  ← Redireciona para /home
```

---

## 🗄️ Banco de Dados — MongoDB Atlas (Nuvem)

O projeto usa o **MongoDB Atlas** (banco na nuvem). **Não é necessário instalar MongoDB localmente** para rodar a aplicação.

### Como configurar o acesso ao banco

1. **Crie o arquivo `application.properties`** na pasta `backend/src/main/resources/` copiando o exemplo:

```bash
# Windows (PowerShell)
Copy-Item backend\src\main\resources\application.properties.example backend\src\main\resources\application.properties

# Linux / macOS
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
```

2. **Preencha com as credenciais do Atlas.**

> ⚠️ **As credenciais de acesso (usuário e senha) não ficam no repositório por segurança.**
> **Entre em contato com a Ana Paula pelo WhatsApp para receber a string de conexão completa.**

> O arquivo `application.properties` está no `.gitignore` e **nunca deve ser commitado** — ele contém senhas reais.

### Como acessar o cluster do Atlas (membros do grupo)

O cluster se chama **biblioteca-cluster** e está na organização **biblioteca-QS** no MongoDB Atlas.

**Passos para acessar:**

1. Acesse [https://cloud.mongodb.com](https://cloud.mongodb.com)
2. Faça login com sua conta MongoDB Atlas
3. Você deve ter recebido um **convite por e-mail** para a organização — aceite o convite
4. Selecione a organização **"Organização de Ana..."** → projeto **"biblioteca-QS"**
5. Clique em **"biblioteca-cluster"** → **"Browse Collections"** para ver os dados

### Como fazer login no banco via MongoDB Compass (interface gráfica)

O MongoDB Compass é um programa visual para ver e editar os dados do banco diretamente.

**Instalação:**
1. Baixe o [MongoDB Compass](https://www.mongodb.com/try/download/compass) e instale normalmente
2. Abra o programa após a instalação

**Conexão:**
1. Na tela inicial, clique em **"New Connection"** (Nova Conexão)
2. No campo **"URI"**, cole a string de conexão fornecida pela Ana Paula pelo WhatsApp
   - A string tem o formato: `mongodb+srv://usuario:senha@biblioteca-cluster.xxxxx.mongodb.net/`
3. Clique em **"Connect"** (Conectar)
4. Aguarde a conexão — você verá os bancos disponíveis na barra lateral esquerda

**Navegando nos dados:**
- Clique em **`biblioteca_db`** para ver os dados reais da aplicação
- Clique em **`livros`** para ver todos os livros cadastrados
- Clique em **`usuarios`** para ver as contas de usuário
- Ignore o banco **`test`** — ele é criado automaticamente pelo MongoDB e não é usado

### Estrutura do banco `biblioteca_db`

| Coleção | Campos principais | Descrição |
|---|---|---|
| `livros` | `title`, `author`, `cover`, `excerpt`, `status`, `language`, `pages`, `categories`, `publisher`, `publishedDate` | Todos os livros cadastrados pelos usuários |
| `usuarios` | `nome`, `email`, `senhaHash`, `role`, `cep`, `logradouro`, `bairro`, `cidade`, `uf`, `enderecos`, `telefones`, `redesSociais` | Contas de usuário com hash de senha (nunca a senha em texto puro) |

---

## 🛠 Tecnologias e Versões

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Backend |
| Spring Boot | 4.x | Framework backend |
| MongoDB Atlas | 7.0 | Banco de dados na nuvem |
| React + Vite | 18+ | Frontend |
| Node.js | 18+ | Runtime do frontend |
| JUnit 5 | - | Testes automatizados |
| Testcontainers | 1.19.7 | MongoDB real nos testes (requer Docker) |
| JaCoCo | 0.8.12 | Relatório de cobertura de testes |
| WireMock | 3.5.2 | Simulação de API externa (ViaCEP) nos testes |
| SonarCloud | - | Análise de qualidade de código |
| GitHub Actions | - | CI/CD automatizado |

---

## ⚙️ Pré-requisitos

Para rodar este projeto na sua máquina:

1. **Java JDK 17** — verifique com: `java -version`
   - Download: https://adoptium.net/
2. **Node.js v18+** — verifique com: `node -v`
   - Download: https://nodejs.org/
3. **Docker** — necessário **apenas para rodar os testes automatizados localmente**
   - Windows: [Docker Desktop](https://www.docker.com/products/docker-desktop/) — deve estar **aberto e rodando** antes de executar os testes
   - Linux: Docker Engine — verifique com: `docker ps`
   - ⚠️ **Para rodar a aplicação em si, Docker NÃO é necessário** — o banco está no MongoDB Atlas
   - ⚠️ **Para o GitHub Actions, Docker NÃO é necessário** — o servidor já tem Docker instalado
4. **Git** — verifique com: `git --version`
   - Download: https://git-scm.com/

**Opcional (para visualizar os dados do banco):**
- **MongoDB Compass** — interface gráfica para o banco: https://www.mongodb.com/try/download/compass

---

## 🚀 Como Executar o Projeto

Clone o repositório:

```bash
git clone https://github.com/AnaPaula2024/biblioteca.git
cd biblioteca
```

Configure o banco de dados conforme a seção "MongoDB Atlas" acima (crie o `application.properties` com as credenciais recebidas pelo WhatsApp).

### 1. Rodando o Backend

**Windows:**
```bash
cd backend
mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
cd backend
./mvnw spring-boot:run
```

O backend estará rodando em: **http://localhost:8080**

### 2. Rodando o Frontend

```bash
cd frontend
npm install
npm run dev
```

O frontend estará acessível em: **http://localhost:5173**

---

## 🧪 Como Rodar os Testes e Gerar Relatório de Cobertura

> ⚠️ **Requer Docker rodando** — os testes usam Testcontainers para subir um MongoDB real e efêmero durante os testes, garantindo fidelidade sem usar Mocks.

**Windows:**
```bash
cd backend
mvnw.cmd clean test
```

**Linux / macOS:**
```bash
cd backend
./mvnw clean test
```

Se todos os testes passarem, você verá `BUILD SUCCESS` no terminal.

### 📊 Visualizando o Relatório JaCoCo

Após `BUILD SUCCESS`, o relatório HTML de cobertura é gerado automaticamente:

1. Navegue até a pasta: `backend/target/site/jacoco/`
2. Abra o arquivo `index.html` no seu navegador
3. Você verá a porcentagem de cobertura por classe, método e linha

A cobertura atual é de **96%** (meta mínima exigida: 80%).

---

## 🔍 Qualidade de Código — SonarCloud

O projeto está integrado ao SonarCloud para análise automática de qualidade de código. A análise está configurada neste fork (`AnaPaula2024/biblioteca`) pois o SonarCloud exige vínculo com uma conta pessoal do GitHub — não é possível configurar no repositório de outra pessoa sem ser administrador.

**O link é público** — qualquer pessoa do grupo pode acessar sem precisar de login:
- https://sonarcloud.io/project/overview?id=AnaPaula2024_biblioteca

A análise roda automaticamente a cada push via GitHub Actions.

---

## 🔄 CI/CD — GitHub Actions

O pipeline de CI roda automaticamente a cada push no repositório:

- **Arquivo de configuração:** `.github/workflows/ci.yml`
- **O que executa:** compilação do projeto, todos os testes, geração do relatório JaCoCo e análise do SonarCloud
- **Histórico de execuções:** https://github.com/AnaPaula2024/biblioteca/actions

O histórico de execuções fica neste fork. O repositório original (`limmuz/biblioteca`) tem seu próprio histórico separado.

**Como verificar se o CI passou:**
1. Acesse https://github.com/AnaPaula2024/biblioteca/actions
2. Clique no workflow mais recente
3. Se estiver verde (✅), todos os testes passaram e o SonarCloud foi atualizado
4. Se estiver vermelho (❌), clique para ver o log de erro
