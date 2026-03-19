Projeto Lybre - Guia de Início Rápido
Este guia contém os passos necessários para configurar e rodar o projeto localmente.


**********************************************
 Pré-requisitos
Antes de começar, certifique-se de ter instalado:

Java 17 ou superior.

Node.js (versão 18 ou superior).

MongoDB rodando localmente na porta 27017 (ou via Docker).

Maven (opcional, pois usamos o mvnw).
**********************************************
*******1. Backend (Spring Boot)
O servidor roda na porta 8080.

Navegue até a pasta do backend:

Bash
cd backend
Instale as dependências e rode o projeto:

Bash
./mvnw spring-boot:run
O banco de dados biblioteca será criado automaticamente no MongoDB.

*******2. Frontend (React + Vite)
O site roda na porta 5173.

Navegue até a pasta do frontend:

Bash
cd frontend
Instale as bibliotecas (apenas na primeira vez):

Bash
npm install
Inicie o painel de desenvolvimento:

Bash
npm run dev
*******3. Banco de Dados (MongoDB)
URI Padrão: mongodb://localhost:27017

Nome da Database: biblioteca

Coleção principal: livros

Nota: Se o backend não conectar, verifique se o serviço do MongoDB está ativo:
sudo systemctl status mongodb (Linux) ou no Compass (Windows).

*******Fluxo de Desenvolvimento
Ligar o MongoDB.

Rodar o Backend (Aguarde o log: Started BibliotecaApplication).

Rodar o Frontend (Clique no link do terminal: http://localhost:5173).
********************************************************
Dicas Importantes
Pesquisa: A busca no Header utiliza a API da Open Library.

IDs de Livros: Se encontrar erros de navegação, certifique-se de que os IDs não contenham barras /.

CORS: O backend está configurado para aceitar requisições apenas de http://localhost:5173.