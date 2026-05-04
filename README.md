# 📚 Lybre — Sistema de Biblioteca Pessoal

Sistema de gerenciamento de biblioteca pessoal com autenticação JWT, CRUD de livros, validações de regras de negócio e interface moderna. Desenvolvido com Spring Boot (backend) e React + Vite (frontend), usando **MongoDB Atlas** como banco de dados na nuvem.

---

## 🆕 Últimas Atualizações

- ✅ Mensagem "Nenhum livro sendo lido" centralizada e maior na tela inicial
- ✅ Validação de livro duplicado: backend retorna erro 409 e frontend exibe mensagem clara
- ✅ Arquivos sensíveis (senhas) removidos do repositório — `.gitignore` atualizado
- ✅ Banco migrado para **MongoDB Atlas** (nuvem) — sem necessidade de Docker para rodar a aplicação
- ✅ Perfil do usuário com múltiplos endereços, telefones e redes sociais
- ✅ Página de detalhes do livro com layout corrigido

---

## 🗄️ Banco de Dados — MongoDB Atlas (Nuvem)

O projeto usa o **MongoDB Atlas** (banco na nuvem). **Não é necessário instalar MongoDB localmente** para rodar a aplicação.

### Como configurar o acesso ao banco

1. **Crie o arquivo `application.properties`** na pasta `backend/src/main/resources/` copiando o exemplo:

```bash
# Linux / macOS
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties

# Windows (PowerShell)
Copy-Item backend\src\main\resources\application.properties.example backend\src\main\resources\application.properties
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

1. Baixe o [MongoDB Compass](https://www.mongodb.com/try/download/compass)
2. Clique em **"New Connection"**
3. Use a string de conexão fornecida pela Ana Paula pelo WhatsApp
4. Clique em **"Connect"**
5. Você verá os bancos: **biblioteca_db** (dados reais) e **test** (ignorar)

### Estrutura do banco `biblioteca_db`

| Coleção | Descrição |
|---|---|
| `livros` | Todos os livros cadastrados pelos usuários |
| `usuarios` | Contas de usuário com hash de senha |

---

## ⚠️ ATENÇÃO: Complementar RTM.md

O arquivo `RTM.md` já existe no projeto, mas **PRECISA SER COMPLEMENTADO** com:

### 📝 O que falta no RTM.md:

1. **Diagramas UML de Sequência** (OBRIGATÓRIO)
   - Criar diagrama para CADA requisito funcional (RF-01 a RF-08)
   - Detalhar o fluxo completo de cada operação
   - Mostrar interação entre Frontend → Backend → MongoDB
   - **Ferramentas sugeridas:** PlantUML, Draw.io, Lucidchart

2. **Preencher Arquivos de Teste**
   - ✅ **Testes já existem** no diretório `backend/src/test/java/com/qs/biblioteca/`
   - **Testes E2E:** `AuthE2ETest.java`, `LivroE2ETest.java`
   - **Testes de Integração:** `LivroServiceIntegrationTest.java`
   - **Testes Unitários:** `LivroValidatorParamTest.java`
   - **Ação necessária:** Associar cada teste ao seu requisito funcional no RTM.md
   - Exemplo: `AuthE2ETest.java` → RF-01 (Cadastro) e RF-02 (Login)

3. **Evidências de Qualidade**
   - Adicionar link do relatório JaCoCo (após rodar testes)
   - Adicionar link do projeto no SonarCloud
   - Adicionar link do GitHub Actions (pipeline verde)

4. **Atualizar Status**
   - Mudar "Pendente" para "Concluído" conforme for preenchendo
   - Adicionar prints/screenshots como evidência

### 📊 Estrutura dos Diagramas UML Necessários:

Cada diagrama deve mostrar:
- **Ator:** Usuário
- **Frontend:** React (componente específico)
- **Backend:** Controller → Service → Repository
- **Banco:** MongoDB
- **Retorno:** Resposta ao usuário

**Exemplo para RF-01 (Cadastro de Usuário):**
```
Usuário → CadastroPage → api.post('/auth/register')
  → AuthController.register()
  → UsuarioService.registrar()
  → UsuarioRepository.save()
  → MongoDB
  ← Retorna token JWT
  ← Salva sessão no localStorage
  ← Redireciona para /home
```

### ✅ Checklist para Completar o RTM.md:

- [ ] Criar 8 diagramas UML de sequência (um para cada RF)
- [ ] Preencher coluna "Arquivo(s) de teste" com testes reais
- [ ] Adicionar evidências de cobertura (JaCoCo)
- [ ] Adicionar link do SonarCloud
- [ ] Adicionar link do GitHub Actions
- [ ] Atualizar todos os status de "Pendente" para "Concluído"
- [ ] Revisar e validar com o grupo

**Prazo:** Conforme indicado no RTM.md - 19/05/2026

---

## 🛠 Tecnologias e Versões

* **Backend:** Java 17
* **Framework:** Spring Boot 3.x
* **Gerenciador de Dependências:** Maven (Wrapper incluso no projeto)
* **Banco de Dados:** MongoDB Atlas (nuvem)
* **Frontend:** Node.js (v18+) e NPM
* **Testes e Cobertura:** JUnit 5, Testcontainers e JaCoCo (Meta de Cobertura: > 80%)

---

## ⚙️ Pré-requisitos

Para rodar este projeto na sua máquina, você precisará ter instalado:

1. **Java JDK 17** (Verifique com: java -version)
2. **Node.js** (Recomendado v18 LTS ou superior. Verifique com: node -v)
3. **Docker** — necessário **apenas para rodar os testes automatizados localmente** (Testcontainers)
   * *Windows:* Docker Desktop (certifique-se de que o aplicativo está aberto e rodando).
   * *Linux:* Docker Engine (verifique com: docker ps).
   * ⚠️ **Para rodar a aplicação em si, Docker NÃO é necessário** — o banco está no MongoDB Atlas.
   * ⚠️ **Para o GitHub Actions e SonarCloud, Docker NÃO é necessário** — o servidor deles já tem Docker instalado e tudo roda automaticamente.
4. **Git**

---

## 🚀 Como Executar o Projeto

Primeiro, clone o repositório e entre na pasta:

```bash
git clone https://github.com/AnaPaula2024/biblioteca.git
cd biblioteca
```

Em seguida, **configure o banco de dados** conforme a seção "MongoDB Atlas" acima (crie o `application.properties` com as credenciais recebidas pelo WhatsApp).

### 1. Rodando o Backend
Abra um terminal, acesse a pasta backend e use o Maven Wrapper para iniciar o servidor Spring Boot.

No Linux / macOS:
```bash
cd backend
./mvnw spring-boot:run
```

No Windows:
```bash
cd backend
mvnw.cmd spring-boot:run
```

O backend estará rodando em http://localhost:8080.


### 2. Rodando o Frontend
Abra outro terminal, acesse a pasta frontend, instale as dependências e inicie o servidor.

```bash
cd frontend
npm install
npm run dev
```

O frontend estará acessível em: **http://localhost:5173**

---

## 🧪 Como Rodar os Testes e Gerar Relatório de Cobertura

O backend possui uma suíte robusta de testes E2E e de Unidade. A meta de cobertura é de no mínimo 80%. O projeto exige que o Docker esteja rodando, pois utiliza Testcontainers para subir um banco de dados real e efêmero durante os testes, garantindo fidelidade sem usar Mocks.

Abra o terminal na pasta backend:

No Linux / macOS:
./mvnw clean test

No Windows:
mvnw.cmd clean test

### 📊 Visualizando o Relatório JaCoCo
Se todos os testes passarem (BUILD SUCCESS), o relatório HTML de cobertura será gerado automaticamente.

Para visualizar as porcentagens de cobertura do código:
1. Navegue até a pasta: backend/target/site/jacoco/
2. Abra o arquivo index.html no seu navegador de preferência.
