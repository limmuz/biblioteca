# RTM - Matriz de Rastreabilidade de Requisitos

Atualizado em 07/05/2026 — diagramas UML adicionados, WireMock/VCR implementado.

---

## Requisitos Funcionais

| ID | Requisito Funcional | Tipo de teste | Arquivo(s) de teste | Status |
|---|---|---|---|---|
| RF-01 | Cadastrar usuário | E2E / Caixa Preta | `AuthE2ETest.java` (`RegistroTests`) | ✅ Implementado |
| RF-02 | Autenticar usuário (login) | E2E / Caixa Preta | `AuthE2ETest.java` (`LoginTests`) | ✅ Implementado |
| RF-03 | Cadastrar livro | E2E / Caixa Preta | `LivroE2ETest.java` (`CriarLivroTests`) | ✅ Implementado |
| RF-04 | Listar livros | Integração | `LivroE2ETest.java` (`ListarLivrosTests`) + `LivroServiceIntegrationTest.java` | ✅ Implementado |
| RF-05 | Buscar livro por ID | Integração | `LivroE2ETest.java` (`BuscarLivroPorIdTests`) + `LivroServiceIntegrationTest.java` | ✅ Implementado |
| RF-06 | Atualizar livro | Integração | `LivroE2ETest.java` (`AtualizarLivroTests`) + `LivroServiceIntegrationTest.java` | ✅ Implementado |
| RF-07 | Excluir livro | Integração | `LivroE2ETest.java` (`DeletarLivroTests`) + `LivroServiceIntegrationTest.java` | ✅ Implementado |
| RF-08 | Gerenciar sessão (usuário autenticado) | E2E / Caixa Preta | `LivroE2ETest.java` (`UsuarioMeTests`) | ✅ Implementado |
| RF-09 | Consultar CEP via API externa (ViaCEP) | VCR / WireMock | `ViaCepServiceWireMockTest.java` | ✅ Implementado |

---

## Requisitos Não Funcionais

| ID | Eixo | Critério | Evidência | Status |
|---|---|---|---|---|
| RNF-01 | Segurança | Rotas protegidas com JWT + Spring Security | Testes E2E com e sem token | ✅ Implementado |
| RNF-02 | Testabilidade | Cobertura ≥ 80% (JaCoCo) | 96% alcançado — `img/Jacoco.png` | ✅ Implementado |
| RNF-03 | Qualidade | Análise estática com SonarCloud | Pipeline CI verde | ✅ Configurado |
| RNF-04 | CI/CD | Pipeline GitHub Actions executando testes | GitHub Actions — ver link de evidência | ✅ Configurado |
| RNF-05 | Portabilidade | Banco em MongoDB Atlas (sem Docker para rodar o app) | README detalhado | ✅ Implementado |
| RNF-06 | Escalabilidade | Arquitetura separada (Frontend React + Backend Spring Boot) | Estrutura do repositório | ✅ Implementado |

---

## Diagramas UML de Sequência

### RF-01 — Cadastrar Usuário

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as CadastroPage (React)
    participant A as AuthController
    participant S as UsuarioService
    participant V as UsuarioValidator
    participant R as UsuarioRepository
    participant M as MongoDB

    U->>F: Preenche nome, email e senha
    F->>A: POST /api/auth/register {nome, email, senha}
    A->>S: registrar(RegisterRequest)
    S->>V: validarRegistro(request)
    V-->>S: OK (ou lança exceção 400)
    S->>R: findByEmail(email)
    R->>M: query
    M-->>R: null
    R-->>S: Optional.empty()
    S->>R: save(usuario com senha criptografada)
    R->>M: insert
    M-->>R: usuario salvo
    R-->>S: usuario
    S-->>A: AuthResponse{token}
    A-->>F: 200 OK + JWT token
    F->>F: localStorage.setItem("token", jwt)
    F->>U: Redireciona para /home
```

---

### RF-02 — Autenticar Usuário (Login)

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as LoginPage (React)
    participant A as AuthController
    participant S as UsuarioService
    participant R as UsuarioRepository
    participant J as JwtService
    participant M as MongoDB

    U->>F: Informa email e senha
    F->>A: POST /api/auth/login {email, senha}
    A->>S: autenticar(AuthRequest)
    S->>R: findByEmail(email)
    R->>M: query
    M-->>R: usuario
    R-->>S: Optional<Usuario>
    S->>S: verificar senha (BCrypt)
    alt Senha correta
        S->>J: gerarToken(usuario)
        J-->>S: jwt
        S-->>A: AuthResponse{token}
        A-->>F: 200 OK + JWT token
        F->>F: localStorage.setItem("token", jwt)
        F->>U: Redireciona para /home
    else Senha incorreta
        S-->>A: lança AuthException
        A-->>F: 401 Unauthorized
        F->>U: Exibe mensagem de erro
    end
```

---

### RF-03 — Cadastrar Livro

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as NovoLivroPage (React)
    participant L as LivroController
    participant S as LivroService
    participant V as LivroValidator
    participant R as LivroRepository
    participant M as MongoDB

    U->>F: Preenche dados do livro e clica em Salvar
    F->>L: POST /api/livros {titulo, autor, status, ...} + JWT header
    L->>L: verificar JWT (JwtAuthenticationFilter)
    alt JWT inválido
        L-->>F: 401 Unauthorized
    else JWT válido
        L->>S: salvar(livro)
        S->>V: validar(livro)
        V-->>S: OK (ou lança exceção 400)
        S->>R: findByTituloAndAutor(titulo, autor)
        R->>M: query
        M-->>R: null (livro não duplicado)
        S->>R: save(livro)
        R->>M: insert
        M-->>R: livro salvo com id
        R-->>S: Livro
        S-->>L: Livro
        L-->>F: 201 Created + {id, titulo, autor, ...}
        F->>U: Exibe confirmação
    end
```

---

### RF-04 — Listar Livros

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as ListagemPage (React)
    participant L as LivroController
    participant S as LivroService
    participant R as LivroRepository
    participant M as MongoDB

    U->>F: Acessa página de listagem
    F->>L: GET /api/livros?search=termo + JWT header
    L->>L: verificar JWT
    alt JWT inválido
        L-->>F: 401 Unauthorized
    else JWT válido
        L->>S: listarTodos(search)
        alt Com filtro de busca
            S->>R: findByTituloContainingOrAutorContaining(search)
        else Sem filtro
            S->>R: findAll()
        end
        R->>M: query
        M-->>R: lista de livros
        R-->>S: List<Livro>
        S-->>L: List<Livro>
        L-->>F: 200 OK + [...livros]
        F->>U: Exibe lista de livros
    end
```

---

### RF-05 — Buscar Livro por ID

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as DetalheDoLivroPage (React)
    participant L as LivroController
    participant S as LivroService
    participant R as LivroRepository
    participant M as MongoDB

    U->>F: Clica em um livro
    F->>L: GET /api/livros/{id} + JWT header
    L->>L: verificar JWT
    alt JWT inválido
        L-->>F: 401 Unauthorized
    else JWT válido
        L->>S: buscarPorId(id)
        S->>R: findById(id)
        R->>M: query por _id
        alt Livro encontrado
            M-->>R: livro
            R-->>S: Optional<Livro> com valor
            S-->>L: Livro
            L-->>F: 200 OK + {id, titulo, autor, ...}
            F->>U: Exibe detalhes do livro
        else Livro não encontrado
            M-->>R: null
            R-->>S: Optional.empty()
            S-->>L: lança ResourceNotFoundException
            L-->>F: 404 Not Found
            F->>U: Exibe mensagem de erro
        end
    end
```

---

### RF-06 — Atualizar Livro

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as EdicaoDoLivroPage (React)
    participant L as LivroController
    participant S as LivroService
    participant V as LivroValidator
    participant R as LivroRepository
    participant M as MongoDB

    U->>F: Edita campos e clica em Salvar
    F->>L: PUT /api/livros/{id} {titulo, autor, status, ...} + JWT header
    L->>L: verificar JWT
    alt JWT inválido
        L-->>F: 401 Unauthorized
    else JWT válido
        L->>S: atualizar(id, livroAtualizado)
        S->>R: findById(id)
        R->>M: query
        alt Livro não encontrado
            M-->>R: null
            R-->>S: Optional.empty()
            S-->>L: lança ResourceNotFoundException
            L-->>F: 404 Not Found
        else Livro encontrado
            M-->>R: livro existente
            S->>V: validar(livroAtualizado)
            V-->>S: OK
            S->>R: save(livro com novos dados)
            R->>M: update
            M-->>R: livro atualizado
            R-->>S: Livro
            S-->>L: Livro
            L-->>F: 200 OK + {id, titulo, ...}
            F->>U: Exibe confirmação
        end
    end
```

---

### RF-07 — Excluir Livro

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as Frontend (React)
    participant L as LivroController
    participant S as LivroService
    participant R as LivroRepository
    participant M as MongoDB

    U->>F: Clica em Excluir livro
    F->>L: DELETE /api/livros/{id} + JWT header
    L->>L: verificar JWT
    alt JWT inválido
        L-->>F: 401 Unauthorized
    else JWT válido
        L->>S: deletar(id)
        S->>R: findById(id)
        R->>M: query
        alt Livro não encontrado
            M-->>R: null
            R-->>S: Optional.empty()
            S-->>L: lança ResourceNotFoundException
            L-->>F: 404 Not Found
        else Livro encontrado
            M-->>R: livro
            S->>R: deleteById(id)
            R->>M: delete
            M-->>R: OK
            R-->>S: void
            S-->>L: void
            L-->>F: 204 No Content
            F->>U: Remove livro da lista
        end
    end
```

---

### RF-08 — Gerenciar Sessão (Usuário Autenticado)

```mermaid
sequenceDiagram
    actor U as Usuário
    participant F as AppHeader (React)
    participant J as JwtAuthFilter (Spring)
    participant C as UsuarioController
    participant S as UsuarioService
    participant R as UsuarioRepository
    participant M as MongoDB

    U->>F: Acessa qualquer página protegida
    F->>F: localStorage.getItem("token")
    alt Token ausente
        F->>U: Redireciona para /login
    else Token presente
        F->>J: GET /api/usuarios/me (Authorization: Bearer jwt)
        J->>J: validar e extrair email do JWT
        alt JWT expirado ou inválido
            J-->>F: 401 Unauthorized
            F->>F: localStorage.removeItem("token")
            F->>U: Redireciona para /login
        else JWT válido
            J->>C: encaminha requisição com usuário autenticado
            C->>S: buscarPorEmail(email)
            S->>R: findByEmail(email)
            R->>M: query
            M-->>R: usuario
            R-->>S: Optional<Usuario>
            S-->>C: Usuario
            C-->>F: 200 OK + {nome, email, ...}
            F->>U: Exibe nome do usuário no header
        end
    end
```

---

### RF-09 — Consultar CEP via API Externa (VCR / WireMock)

```mermaid
sequenceDiagram
    participant T as ViaCepServiceWireMockTest
    participant W as WireMock Server
    participant S as ViaCepService
    participant V as ViaCEP API (stubbed)

    T->>W: configura stub GET /ws/01310-100/json/
    W-->>T: stub registrado
    T->>S: buscarEnderecoPorCep("01310-100")
    S->>W: GET http://localhost:{porta}/ws/01310-100/json/
    W->>V: (intercepta — não chama API real)
    W-->>S: 200 OK + {cep, logradouro, localidade, uf}
    S-->>T: Map{cep=01310-100, logradouro=Avenida Paulista, ...}
    T->>T: assertThat(resultado).containsEntry(...)
    T->>W: verify(1 chamada para /ws/01310-100/json/)
```

---

## Estratégia de Testes

| Tipo | Arquivo | Ferramenta | Descrição |
|---|---|---|---|
| Unitário parametrizado | `LivroValidatorParamTest.java` | JUnit 5 `@ParameterizedTest` | Valida regras de negócio do `LivroValidator` com 50+ cenários (`@CsvSource`, `@ValueSource`, `@MethodSource`, `@NullAndEmptySource`) |
| Integração | `LivroServiceIntegrationTest.java` | Testcontainers + MongoDB real | Testa `LivroRepository` com banco real e efêmero |
| E2E / Caixa Preta | `AuthE2ETest.java` | Testcontainers + RestTemplate | Fluxo completo de registro e login via HTTP |
| E2E / Caixa Preta | `LivroE2ETest.java` | Testcontainers + RestTemplate | CRUD completo de livros via HTTP com JWT |
| VCR / WireMock | `ViaCepServiceWireMockTest.java` | WireMock 3.5.2 | Testa integração com API externa (ViaCEP) sem dependência da internet |

> ⚠️ **Nenhum Mock (`@Mock`, `@MockBean`, `Mockito.mock()`) foi utilizado.** Todos os testes de persistência usam MongoDB real via Testcontainers, conforme exigido pelo professor.

---

## Cobertura de Código (JaCoCo)

- **Cobertura alcançada: 96%** (mínimo exigido: 80%)
- Relatório gerado com: `./mvnw clean verify`
- Abrir: `backend/target/site/jacoco/index.html`

![Relatório JaCoCo](./img/Jacoco.png)

---

## Evidências de Qualidade

| Item | Link / Evidência | Status |
|---|---|---|
| Cobertura JaCoCo ≥ 80% | `img/Jacoco.png` (96%) | ✅ |
| SonarCloud | https://sonarcloud.io/project/overview?id=AnaPaula2024_biblioteca | ✅ Configurado |
| GitHub Actions (CI) | https://github.com/AnaPaula2024/biblioteca/actions | ⏳ Tirar print após push |
| Testes passando (CI) | Artefato `test-reports` no GitHub Actions | ⏳ Disponível após push |

> **Como obter o print dos testes:**
> 1. Faça `git push` para o GitHub
> 2. Acesse https://github.com/AnaPaula2024/biblioteca/actions
> 3. Aguarde o pipeline ficar verde (≈ 5 minutos)
> 4. Tire print da tela mostrando todos os jobs verdes
> 5. Clique no job `backend` → baixe o artefato `test-reports` para ver o relatório XML dos testes

---

*Revisão final: 19/05/2026*
