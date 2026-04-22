# RTM - Matriz de Rastreabilidade de Requisitos

Status: template inicial preparado em 10/04/2026 para preenchimento final.

## Como preencher

Para cada requisito funcional (RF):
- Informar testes que validam o requisito (unitario, integracao, e2e/caixa preta).
- Informar evidencia (arquivo de teste, print/log de execucao).
- Anexar diagrama UML de sequencia correspondente.

---

## Requisitos Funcionais

| ID | Requisito Funcional | Tipo de teste principal | Arquivo(s) de teste | Status |
|---|---|---|---|---|
| RF-01 | Cadastrar usuario | E2E / Integracao | A definir | Pendente |
| RF-02 | Autenticar usuario (login) | E2E / Integracao | A definir | Pendente |
| RF-03 | Cadastrar livro | E2E / Integracao | A definir | Pendente |
| RF-04 | Listar livros | Integracao | A definir | Pendente |
| RF-05 | Buscar livro por id | Integracao | A definir | Pendente |
| RF-06 | Atualizar livro | Integracao | A definir | Pendente |
| RF-07 | Excluir livro | Integracao | A definir | Pendente |
| RF-08 | Gerenciar sessao no frontend | E2E / Caixa preta | A definir | Pendente |

---

## Requisitos Nao Funcionais (8 eixos)

| ID | Eixo RNF | Criterio de aceitacao | Evidencia esperada | Status |
|---|---|---|---|---|
| RNF-01 | Performance | Respostas em tempo aceitavel | Relatorio de tempo de resposta | Pendente |
| RNF-02 | Seguranca | Rotas protegidas e validacao de acesso | Testes de autenticacao/autorizacao | Pendente |
| RNF-03 | Usabilidade | Fluxo de telas claro e feedback de erro | Validacao manual + checklist UX | Pendente |
| RNF-04 | Confiabilidade | Comportamento estavel sem falhas criticas | Execucoes repetidas de testes | Pendente |
| RNF-05 | Manutenibilidade | Estrutura limpa e padrao consistente | Revisao de codigo + SonarQube | Pendente |
| RNF-06 | Testabilidade | Cobertura minima de 80% e testes automatizados | JaCoCo + pipeline CI | Pendente |
| RNF-07 | Portabilidade | Execucao em ambiente local padrao | README + setup local do MongoDB validados | Pendente |
| RNF-08 | Escalabilidade basica | Arquitetura separada (front/back) e evolutiva | Evidencia da arquitetura e camadas | Pendente |

---

## Diagramas UML de Sequencia (obrigatorio)

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

---

## Evidencias de qualidade

| Item | Evidencia | Status |
|---|---|---|
| Cobertura >= 80% | Relatorio JaCoCo | Pendente |
| Integracao SonarQube | Link/projeto Sonar | Pendente |
| CI GitHub Actions | Link da pipeline verde | Pendente |

---

## Responsavel por finalizar este documento

Colega B (documentacao e qualidade), com revisao final do grupo ate 19/05/2026.
