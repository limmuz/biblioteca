# RTM - Matriz de Rastreabilidade de Requisitos

Atualizado em 04/05/2026 — testes implementados e mapeados. Diagramas UML pendentes.

## Como preencher

Para cada requisito funcional (RF):
- Informar testes que validam o requisito (unitario, integracao, e2e/caixa preta).
- Informar evidencia (arquivo de teste, print/log de execucao).
- Anexar diagrama UML de sequencia correspondente.

---

## Divisão de responsabilidades no RTM

### 🔵 Ana Paula (desenvolvimento, testes e infraestrutura):
- Já implementou todos os testes automatizados (em parceria com Miguel)
- Precisa rodar os testes no computador com Docker e tirar print do resultado
- Precisa verificar se o GitHub Actions ficou verde e tirar print
- Precisa verificar o SonarCloud e tirar print da análise
- Preencher a seção "Evidências de qualidade" com os prints

### 🟢 Integrante responsável pela documentação:
- Criar os 8 diagramas UML de sequência (um para cada RF abaixo)
- Usar PlantUML (https://www.plantuml.com/plantuml/uml/) ou Draw.io (https://app.diagrams.net/)
- Siga o exemplo do RF-01 abaixo
- Salvar os arquivos na pasta `doc/diagramas/` e colocar o link na tabela
- Atualizar os status dos diagramas de "Pendente" para "Concluído"

### 🟡 Integrante responsável pela revisão:
- Revisar todos os itens preenchidos pelos outros dois
- Validar se os diagramas UML estão corretos e completos
- Confirmar que todos os status estão atualizados antes da entrega (19/05/2026)

---

## Requisitos Funcionais

| ID | Requisito Funcional | Tipo de teste principal | Arquivo(s) de teste | Status |
|---|---|---|---|---|
| RF-01 | Cadastrar usuario | E2E / Integracao | `AuthE2ETest.java` (classe `RegistroTests`) | ✅ Teste implementado |
| RF-02 | Autenticar usuario (login) | E2E / Integracao | `AuthE2ETest.java` (classe `LoginTests`) | ✅ Teste implementado |
| RF-03 | Cadastrar livro | E2E / Integracao | `LivroE2ETest.java` (classe `CriarLivroTests`) | ✅ Teste implementado |
| RF-04 | Listar livros | Integracao | `LivroE2ETest.java` (classe `ListarLivrosTests`) + `LivroServiceIntegrationTest.java` | ✅ Teste implementado |
| RF-05 | Buscar livro por id | Integracao | `LivroE2ETest.java` (classe `BuscarLivroPorIdTests`) + `LivroServiceIntegrationTest.java` | ✅ Teste implementado |
| RF-06 | Atualizar livro | Integracao | `LivroE2ETest.java` (classe `AtualizarLivroTests`) + `LivroServiceIntegrationTest.java` | ✅ Teste implementado |
| RF-07 | Excluir livro | Integracao | `LivroE2ETest.java` (classe `DeletarLivroTests`) + `LivroServiceIntegrationTest.java` | ✅ Teste implementado |
| RF-08 | Gerenciar sessao no frontend | E2E / Caixa preta | `LivroE2ETest.java` (classe `UsuarioMeTests`) | ✅ Teste implementado |

---

## Requisitos Nao Funcionais (8 eixos)

| ID | Eixo RNF | Criterio de aceitacao | Evidencia esperada | Status |
|---|---|---|---|---|
| RNF-01 | Performance | Respostas em tempo aceitavel | Relatorio de tempo de resposta | Pendente |
| RNF-02 | Seguranca | Rotas protegidas e validacao de acesso | Testes de autenticacao/autorizacao | ✅ Implementado (JWT + Spring Security) |
| RNF-03 | Usabilidade | Fluxo de telas claro e feedback de erro | Validacao manual + checklist UX | Pendente |
| RNF-04 | Confiabilidade | Comportamento estavel sem falhas criticas | Execucoes repetidas de testes | Pendente |
| RNF-05 | Manutenibilidade | Estrutura limpa e padrao consistente | Revisao de codigo + SonarCloud | Pendente — aguarda pipeline verde |
| RNF-06 | Testabilidade | Cobertura minima de 80% e testes automatizados | JaCoCo + pipeline CI | ✅ 96% de cobertura alcançada |
| RNF-07 | Portabilidade | Execucao em ambiente local padrao | README + setup local do MongoDB validados | ✅ README atualizado com instruções do Atlas |
| RNF-08 | Escalabilidade basica | Arquitetura separada (front/back) e evolutiva | Evidencia da arquitetura e camadas | ✅ Frontend React + Backend Spring Boot separados |

---

## Diagramas UML de Sequencia (obrigatorio)

> ⚠️ **Responsável: integrante de documentação** — criar os diagramas e colocar o link abaixo

| Requisito | Link/arquivo do diagrama | Status |
|---|---|---|
| RF-01 Cadastro de usuario | A definir | Pendente |
| RF-02 Login | A definir | Pendente |
| RF-03 Cadastro de livro | A definir | Pendente |
| RF-04 Listagem de livros | A definir | Pendente |
| RF-05 Busca por id | A definir | Pendente |
| RF-06 Atualizacao de livro | A definir | Pendente |
| RF-07 Exclusao de livro | A definir | Pendente |
| RF-08 Gerenciamento de sessao | A definir | Pendente |

### Exemplo de diagrama para RF-01 (Cadastro de Usuário):
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

### Ferramentas sugeridas para criar os diagramas:
- **PlantUML** (online: https://www.plantuml.com/plantuml/uml/)
- **Draw.io** (online: https://app.diagrams.net/)
- **Lucidchart** (online: https://www.lucidchart.com/)

---

## Evidencias de qualidade

> ⚠️ **Responsável: Ana Paula** — rodar os testes e preencher com prints

| Item | Evidencia | Status |
|---|---|---|
| Cobertura >= 80% | Relatório JaCoCo gerado pelo Miguel (96% — ver imagem abaixo) | ✅ Evidência disponível |
| Integracao SonarCloud | https://sonarcloud.io/project/overview?id=AnaPaula2024_biblioteca | ✅ Configurado — tirar print após CI verde |
| CI GitHub Actions | https://github.com/AnaPaula2024/biblioteca/actions | Pendente — aguarda pipeline verde |

### Resultado do JaCoCo (96% de cobertura — gerado pelo Miguel):

```
Pacote                          | Cobertura | Branches
com.qs.biblioteca.integration  |    91%    |   83%
com.qs.biblioteca.security     |    95%    |   57%
com.qs.biblioteca.service      |    96%    |  100%
com.qs.biblioteca               |    37%    |   n/a
com.qs.biblioteca.controller   |    97%    |   71%
com.qs.biblioteca.config       |   100%    |   n/a
com.qs.biblioteca.unit         |   100%    |  100%
com.qs.biblioteca.model        |   100%    |   n/a
─────────────────────────────────────────────────
Total                          |    96%    |   84%
(34 de 954 instruções perdidas, 11 de 72 branches)
```

> Para gerar o relatório HTML completo localmente, execute com Docker rodando:
> - Windows: `cd backend && mvnw.cmd clean test`
> - Linux/macOS: `cd backend && ./mvnw clean test`
> - Abra: `backend/target/site/jacoco/index.html`

---

## Responsavel por finalizar este documento

Revisão final do grupo até 19/05/2026.
