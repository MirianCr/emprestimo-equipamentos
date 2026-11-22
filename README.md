# Sistema de Empréstimo de Equipamentos

API REST para controlar o empréstimo de equipamentos (notebooks, monitores, headsets) para as pessoas colaboradoras: quem está com o quê e o que está atrasado.

**Stack:** Java 25 · Spring Boot 4.1 · Spring Data JPA · H2 (em memória) · JUnit 5

- **Repositório:** `<https://github.com/MirianCr/emprestimo-equipamentos>`
- **API no Render:** `<https://emprestimo-equipamentos-bnr4.onrender.com>`

## Como rodar

Pré-requisito: JDK 25 ou mais novo e Maven (ou o IntelliJ, que já traz o Maven).

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. No IntelliJ, basta rodar a classe `EmprestimosApplication`.

| O quê | Endereço |
|---|---|
| Swagger (documentação e testes das rotas) | http://localhost:8080/swagger-ui/index.html |
| Console do H2 | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:emprestimos`, usuário `sa`, senha vazia) |
| Health check | http://localhost:8080/actuator/health |

### Testes

```bash
mvn test
```

Roda os testes unitários das entidades (`EquipamentoTest`, `EmprestimoTest`) e os testes de integração com MockMvc (`EquipamentoControllerTest`, `EmprestimoControllerTest`).

### Docker

```bash
docker build -t emprestimos .
docker run -p 8080:8080 emprestimos
```

## Rotas

### Equipamentos

| Método | Rota | O que faz | Sucesso |
|---|---|---|---|
| GET | `/equipamentos` | Lista; filtros opcionais `?tipo=NOTEBOOK\|MONITOR\|HEADSET` e `?disponivel=true\|false` | 200 |
| GET | `/equipamentos/{id}` | Busca um equipamento | 200 |
| POST | `/equipamentos` | Cadastra (`{"patrimonio", "nome", "tipo"}`) | 201 |
| PUT | `/equipamentos/{id}` | Atualiza só `nome` e `tipo` | 200 |
| DELETE | `/equipamentos/{id}` | Remove (se nunca teve empréstimo) | 204 |

### Colaboradores

| Método | Rota | O que faz | Sucesso |
|---|---|---|---|
| POST | `/colaboradores` | Cadastra (`{"nome", "email", "cargo"}`) | 201 |
| GET | `/colaboradores` | Lista | 200 |
| GET | `/colaboradores/{id}` | Busca uma pessoa | 200 |
| GET | `/colaboradores/{id}/emprestimos` | Histórico de empréstimos da pessoa | 200 |

### Empréstimos

| Método | Rota | O que faz | Sucesso |
|---|---|---|---|
| POST | `/emprestimos?equipamentoId=1&colaboradorId=2` | Empresta | 201 |
| GET | `/emprestimos` | Lista todos; com `?atrasados=true`, só os atrasados | 200 |
| GET | `/emprestimos/{id}` | Busca um empréstimo | 200 |
| PUT | `/emprestimos/{id}/devolucao` | Registra a devolução | 200 |

O JSON do empréstimo traz o campo calculado `"atrasado"` (não é coluna no banco).

## Regras de negócio

| Regra | O que diz | Onde mora |
|---|---|---|
| R1 | Não existem dois equipamentos com o mesmo patrimônio | `EquipamentoService.cadastrar` |
| R2 | Todo equipamento novo nasce disponível, mesmo que o JSON mande `"disponivel": false` | `EquipamentoService.cadastrar` |
| R3 | Não existem duas pessoas com o mesmo e-mail | `ColaboradorService.cadastrar` |
| R4 | Só se empresta equipamento disponível | `Equipamento.emprestar` |
| R5 | Ao emprestar: equipamento indisponível, empréstimo hoje, devolução prevista em 14 dias | construtor de `Emprestimo` + `Equipamento.emprestar` + `EmprestimoService.emprestar` |
| R6 | Empréstimo já devolvido não pode ser devolvido de novo | `Emprestimo.devolver` |
| R7 | Ao devolver: data de devolução é hoje e o equipamento volta a ficar disponível | `Emprestimo.devolver` + `Equipamento.devolver` + `EmprestimoService.devolver` |
| R8 | Atrasado = não devolvido e com a data prevista já passada (calculado na hora) | `Emprestimo.estaAtrasadoEm` + filtro do repository |
| R9 | Equipamento que já teve empréstimo não pode ser removido | `EquipamentoService.remover` |
| R10 | Quem está com empréstimo atrasado não pode pegar outro equipamento | `EmprestimoService.emprestar` |

`emprestar` e `devolver` no service são `@Transactional`: se uma regra falhar no meio, nada fica salvo pela metade.

Para testar R8 e R10 sem esperar 14 dias, atrase um empréstimo pelo console do H2:

```sql
UPDATE EMPRESTIMO SET DATA_PREVISTA_DEVOLUCAO = '2026-11-01' WHERE ID = 2;
```

## Erros

| Situação | Status |
|---|---|
| Equipamento, colaborador ou empréstimo que não existe | 404 |
| R1, R3: patrimônio ou e-mail repetido | 409 |
| R4, R6, R9, R10: a operação conflita com o estado atual | 409 |
| Dados inválidos (campo obrigatório faltando, e-mail inválido) | 400, com a lista dos campos |

Exemplo de corpo de erro:

```json
{"timestamp": "...", "status": 409, "erro": "Conflict", "mensagem": "Equipamento indisponível"}
```

## Deploy no Render

1. `server.port=${PORT:8080}` já está no `application.properties` e o `Dockerfile` está na raiz.
2. Em render.com: **New + → Web Service** → escolher o repositório → Language **Docker**, Branch `main`, Instance Type **Free**.
3. Em **Environment**, adicionar `SPRING_H2_CONSOLE_ENABLED=false`.
4. Em **Settings → Health Check Path**, colocar `/actuator/health`.

No plano grátis o serviço dorme após 15 minutos parado, e o H2 perde os dados a cada reinício.

## Bônus feitos

- [x] **N1 – Swagger** (`springdoc-openapi`, com `SwaggerConfig`)
- [x] **N1 – Actuator** (`/actuator/health`)
- [x] **N2 – Validação** (`@NotBlank`, `@NotNull`, `@Email` + `@Valid`)
- [x] **N2 – Tratamento de erros** (`NaoEncontradoException`, `ConflitoException`, `TratadorDeErros` com 404/409/400)
- [x] **N2 – Testes de integração com MockMvc**
- [x] **N3 – GitHub Actions** (`.github/workflows/ci.yml` roda `mvn test` a cada push)
- [ ] N2 – `@ManyToOne` (não feito: o empréstimo guarda os ids como `Long`, como no obrigatório)
- [ ] N3 – Spring Security com JWT (não feito)
