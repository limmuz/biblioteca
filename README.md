# 📚 Projeto Biblioteca

Sistema de gerenciamento de biblioteca com autenticação JWT, CRUD de livros e validações de regras de negócio. O projeto foi desenvolvido com separação de responsabilidades entre Backend (Spring Boot) e Frontend, utilizando MongoDB como banco de dados.

## 🛠 Tecnologias e Versões

* **Backend:** Java 17
* **Framework:** Spring Boot 3.x
* **Gerenciador de Dependências:** Maven (Wrapper incluso no projeto)
* **Banco de Dados:** MongoDB
* **Frontend:** Node.js (v18+) e NPM
* **Testes e Cobertura:** JUnit 5, Testcontainers e JaCoCo (Meta de Cobertura: > 80%)

---

## ⚙️ Pré-requisitos

Para rodar este projeto na sua máquina, você precisará ter instalado:

1. **Java JDK 17** (Verifique com: java -version)
2. **Node.js** (Recomendado v18 LTS ou superior. Verifique com: node -v)
3. **Docker** (Essencial para rodar o MongoDB e os Testcontainers)
   * *Windows:* Docker Desktop (certifique-se de que o aplicativo está aberto e rodando).
   * *Linux:* Docker Engine (verifique com: docker ps).
4. **Git**

---

## 🚀 Como Executar o Projeto

Primeiro, clone o repositório e entre na pasta:

git clone https://github.com/limmuz/biblioteca.git
cd biblioteca


### 1. Banco de Dados (MongoDB)
Para rodar a aplicação localmente, você precisa de uma instância do MongoDB rodando no Docker na porta padrão (27017).

Linux e Windows (via terminal):
docker run -d -p 27017:27017 --name biblioteca-mongo mongo:latest

*(Nota: Para os testes automatizados, o banco subirá sozinho usando Testcontainers, você não precisa fazer isso).*


### 2. Rodando o Backend
Abra um terminal, acesse a pasta backend e use o Maven Wrapper para iniciar o servidor Spring Boot.

No Linux / macOS:
cd backend
./mvnw spring-boot:run

No Windows:
cd backend
mvnw.cmd spring-boot:run

O backend estará rodando em http://localhost:8080.


### 3. Rodando o Frontend
Abra outro terminal, acesse a pasta frontend, instale as dependências e inicie o servidor.

Linux e Windows:
cd frontend
npm install
npm start

*(Se o seu frontend usar Vite, o comando pode ser npm run dev).*
O frontend estará acessível no seu navegador (geralmente em http://localhost:3000 ou 5173).

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