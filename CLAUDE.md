# CLAUDE.md — Baile dos Namorados

Guia de referência rápida para desenvolvedores e IAs que abrem o projeto pela primeira vez.

---

## 1. Visão geral do projeto

Sistema web de controle de lista de presença e pagamento para o **Baile dos Namorados** da Igreja Nossa Senhora do Perpétuo Socorro. O operador (único usuário autenticado) cadastra participantes, informa se a reserva é de casal ou individual, e registra o pagamento com um clique. A tela principal exibe a lista completa com filtros e um painel de resumo financeiro.

Não há cadastro público nem múltiplos usuários — é uma ferramenta interna de gestão do evento.

**Funcionalidades principais:**
- Cadastro de reservas (casal R$ 100 / individual R$ 50) com valor calculado automaticamente
- Toggle de pagamento via **checkbox direto na linha** da tabela
- **Edição de cadastro via modal client-side** (`data-*` attributes, sem rota GET ao servidor)
- Filtros por status (todos / pagos / pendentes) e busca por nome
- **Dois relatórios distintos:** Financeiro (resumo + tabelas) e Lista de Entrada (porta do evento, otimizada para impressão)

---

## 2. Stack e versões

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.5 |
| Spring Security | 6 (via Boot) |
| Spring Data JPA / Hibernate | via Boot 3.2.5 |
| Thymeleaf | via Boot 3.2.5 |
| thymeleaf-extras-springsecurity6 | via Boot |
| Bean Validation (Jakarta) | via Boot |
| Lombok | via Boot |
| PostgreSQL (driver) | via Boot |
| PostgreSQL (banco) | 16-alpine (Docker) |
| Maven | Wrapper incluído |
| Docker / Docker Compose | v3.9 |

---

## 3. Estrutura de pacotes

Pacote raiz: `com.igreja.baile`

```
com.igreja.baile
├── BaileNamoradosApplication.java   # entry point (@SpringBootApplication)
├── controller/
│   ├── LoginController.java         # GET /login — exibe tela de login
│   └── ReservaController.java       # todas as rotas de reservas e dashboard
├── dto/
│   ├── ReservaForm.java             # form object com validações (@NotBlank, @NotNull)
│   └── ResumoDTO.java               # projeção de totais para o painel resumo
├── model/
│   └── Reserva.java                 # entidade JPA + enum TipoReserva (inner)
├── repository/
│   └── ReservaRepository.java       # Spring Data JPA + queries JPQL customizadas
├── security/
│   └── SecurityConfig.java          # configuração Spring Security, BCrypt, usuário em memória
└── service/
    └── ReservaService.java          # regras de negócio, cálculo de valor, toggle de pagamento
```

**Templates Thymeleaf** (`src/main/resources/templates/`):

```
templates/
├── index.html             # dashboard principal (lista, filtros, modal criar, modal editar)
├── login.html             # página de login
└── relatorio/
    ├── financeiro.html    # relatório financeiro (cards + tabelas pagos/pendentes)
    └── entrada.html       # lista de entrada para a porta do evento
```

---

## 4. Arquitetura

**Padrão:** MVC server-side. Sem API REST — tudo renderizado pelo Thymeleaf no servidor.

**Fluxo de uma requisição:**

```
Browser → Controller (@Controller)
            ↓
          Service (@Service)   ← regras de negócio, transações
            ↓
          Repository (JpaRepository)
            ↓
          PostgreSQL
            ↓
          (resultado volta pela cadeia)
            ↓
          Controller adiciona atributos ao Model
            ↓
          Thymeleaf renderiza template HTML
            ↓
          Browser recebe HTML pronto
```

**Form DTOs:** O controller recebe `ReservaForm` (não a entidade diretamente). O service converte o form em entidade antes de persistir.

**Redirecionamento pós-POST:** após salvar, excluir ou fazer toggle, o controller usa `redirect:/` com `RedirectAttributes` para flash messages — evita resubmissão de formulário no refresh (PRG pattern).

---

## 5. Modelos de dados

### Entidade `Reserva` — tabela `reservas`

| Campo | Tipo Java | Tipo DB | Regra |
|---|---|---|---|
| `id` | `Long` | BIGSERIAL | PK, gerado automaticamente |
| `nome` | `String` | VARCHAR | NOT NULL |
| `tipo` | `TipoReserva` | VARCHAR | NOT NULL, armazenado como string ("CASAL" / "INDIVIDUAL") |
| `valor` | `BigDecimal` | NUMERIC | NOT NULL, preenchido automaticamente pelo service |
| `pago` | `Boolean` | BOOLEAN | NOT NULL, padrão `false` |
| `dataPagamento` | `LocalDateTime` | TIMESTAMP | nullable; preenchido automaticamente no momento do pagamento |
| `criadoEm` | `LocalDateTime` | TIMESTAMP | NOT NULL, preenchido no `@PrePersist`, imutável |
| `atualizadoEm` | `LocalDateTime` | TIMESTAMP | atualizado no `@PreUpdate` |

### Enum `TipoReserva` (inner class de `Reserva`)

```java
CASAL      // R$ 100,00
INDIVIDUAL // R$  50,00
```

### Regras de negócio

- O **valor é atribuído automaticamente** pelo service com base no tipo — o operador não digita valor.
- Valores configuráveis em `application.properties`: `app.valor.casal=100.00` e `app.valor.individual=50.00`.
- Ao **marcar como pago** (toggle ou form), `dataPagamento` é definida para `LocalDateTime.now()`.
- Ao **desmarcar o pagamento**, `dataPagamento` volta a `null`.
- O schema é criado/atualizado automaticamente pelo Hibernate (`ddl-auto=update`).

---

## 6. Rotas da aplicação

### GET

| Rota | Descrição |
|---|---|
| `GET /login` | Exibe página de login; aceita query params `?erro=true` e `?saiu=true` |
| `GET /` | Dashboard principal com lista de reservas, painel resumo e formulário de cadastro |
| `GET /?busca={texto}` | Filtra reservas por nome (case-insensitive, contains) |
| `GET /?filtro=pagos` | Exibe apenas reservas pagas |
| `GET /?filtro=pendentes` | Exibe apenas reservas pendentes |
| `GET /relatorio/financeiro` | Resumo financeiro: cards de totais + tabela de pagos + tabela de pendentes |
| `GET /relatorio/entrada` | Lista completa ordenada por nome com coluna de confirmação, otimizada para impressão |

### POST

| Rota | Descrição |
|---|---|
| `POST /login` | Processado pelo Spring Security (não há método no controller) |
| `POST /logout` | Processado pelo Spring Security |
| `POST /reservas/salvar` | Cria ou atualiza reserva (id null = criar, id preenchido = editar) |
| `POST /reservas/{id}/toggle-pagamento` | Alterna pago/pendente e atualiza dataPagamento |
| `POST /reservas/{id}/excluir` | Remove a reserva permanentemente |

---

## 7. Segurança

- **Único usuário:** configurado em memória via `InMemoryUserDetailsManager`. Não há tabela de usuários no banco.
- **Senha:** codificada com `BCryptPasswordEncoder` a cada inicialização da aplicação.
- **Credenciais padrão (desenvolvimento):** usuário `admin`, senha `baile@2025`.
- **Em produção:** sobrescrever via env vars `ADMIN_USER` e `ADMIN_PASS` (ver seção 8).
- **Login:** Spring Security form login padrão, página customizada em `/login`.
- **Sucesso:** redireciona para `/`.
- **Falha:** redireciona para `/login?erro=true`.
- **Logout:** redireciona para `/login?saiu=true`.
- **Recursos públicos:** `/css/**`, `/js/**`, `/favicon.ico` — liberados sem autenticação.
- **Tudo o mais:** requer autenticação (role `ADMIN`).

---

## 8. Configuração e variáveis de ambiente

Arquivo: `src/main/resources/application.properties`

| Propriedade | Env var | Padrão local | Descrição |
|---|---|---|---|
| `spring.datasource.url` | `DB_URL` | `jdbc:postgresql://localhost:5432/baile_namorados` | URL de conexão JDBC |
| `spring.datasource.username` | `DB_USER` | `baile` | Usuário do banco |
| `spring.datasource.password` | `DB_PASS` | `baile123` | Senha do banco |
| `app.admin.username` | `ADMIN_USER` | `admin` | Login do painel |
| `app.admin.password` | `ADMIN_PASS` | `baile@2025` | Senha do painel |
| `server.port` | `PORT` | `8080` | Porta HTTP da aplicação |

Propriedades fixas em `application.properties` (sem env var correspondente, editáveis direto no arquivo):

| Propriedade | Valor padrão | Descrição |
|---|---|---|
| `app.evento.nome` | `Baile dos Namorados` | Nome do evento (exibido nos templates) |
| `app.evento.local` | `Igreja Nossa Senhora do Perpétuo Socorro` | Local do evento |
| `app.valor.casal` | `100.00` | Valor cobrado por reserva do tipo CASAL |
| `app.valor.individual` | `50.00` | Valor cobrado por reserva do tipo INDIVIDUAL |

Variáveis usadas apenas no `docker-compose.yml` (não mapeadas no `application.properties`):

| Env var | Padrão | Descrição |
|---|---|---|
| `APP_PORT` | `8080` | Porta exposta no host (lado esquerdo do bind) |

---

## 9. Como rodar localmente

### Pré-requisitos

- Java 17+
- Maven (ou use o wrapper `./mvnw`)
- Docker (para subir apenas o PostgreSQL)

### 1. Subir apenas o banco com Docker

```bash
docker run -d \
  --name baile-db \
  -e POSTGRES_DB=baile_namorados \
  -e POSTGRES_USER=baile \
  -e POSTGRES_PASSWORD=baile123 \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

Acesse: `http://localhost:8080` — login: `admin` / `baile@2025`

### 3. Rodar pela IDE (IntelliJ / Eclipse)

Execute a classe `BaileNamoradosApplication.java` como aplicação Java. O banco deve estar rodando em `localhost:5432` com as credenciais padrão.

---

## Observações de setup

- **Maven Wrapper (`mvnw`) não é gerado automaticamente** pelo Spring Initializr neste projeto. Caso não exista na raiz, gerar com:
  ```bash
  mvn wrapper:wrapper
  ```
  Após a geração, o arquivo `.mvn/wrapper/maven-wrapper.properties` deve estar presente.

- **Sem o `mvnw`, o comando `./mvnw spring-boot:run` falha** com `no such file or directory`. Sempre verificar a presença do wrapper antes de documentar ou executar comandos que o referenciam.

---

## Armadilhas conhecidas

### Redirect após toggle de pagamento

O parâmetro `referer` enviado pelo form hidden no `index.html` deve sempre conter um path absoluto.

```
ERRADO:  referer = "?filtro=pagos"   → Spring interpreta relativo ao path atual → 404
CORRETO: referer = "/?filtro=pagos"  → path absoluto → redirect funciona
```

Solução adotada: o controller passa `currentQuery` já tratado via `model.addAttribute()`, e o template usa simplesmente:

```html
th:value="${'/' + currentQuery}"
```

Isso garante que toggle com filtro ativo, busca ativa ou sem parâmetros redirecione corretamente para `/`.

### `#httpServletRequest.queryString` é null sem parâmetros

No Thymeleaf, `#httpServletRequest.queryString` retorna `null` quando a URL não tem query string. Qualquer comparação ou concatenação direta com `null` estoura `SpelEvaluationException` em tempo de renderização.

**Solução adotada:** passar o `currentQuery` já tratado via `model.addAttribute()` no controller, evitando acesso direto ao request no template:

```java
// ReservaController.dashboard()
String qs = request.getQueryString();
model.addAttribute("currentQuery", qs != null ? "?" + qs : "");
```

```html
<!-- index.html -->
th:value="${'/' + currentQuery}"
```

### Modal de edição — `data-*` em vez de rota GET

A edição de cadastro é feita **client-side**: o botão ✏️ carrega os dados via atributos `data-id`, `data-nome`, `data-tipo`, `data-pago` e o JavaScript preenche o modal antes de abrir. **Não existe rota `GET /reservas/{id}/editar`.**

Vantagem: sem round-trip ao servidor para abrir o formulário de edição.

Atenção ao escopo do `querySelectorAll` dos radio buttons — usar `#modal-edicao input[name="tipo"]` para não afetar o modal de criação que tem radio buttons com o mesmo `name`.

### 403 Forbidden em POST — token CSRF ausente

O Spring Security exige token CSRF em todo POST. O Thymeleaf injeta o token automaticamente **apenas** quando o atributo do form é `th:action` (com prefixo `th:`). Usar `action=` sem o prefixo não injeta o token e resulta em 403 Forbidden silencioso.

```html
<!-- ERRADO — token CSRF não injetado → 403: -->
<form action="/reservas/salvar" method="post">

<!-- CORRETO — Thymeleaf injeta _csrf automaticamente: -->
<form th:action="@{/reservas/salvar}" method="post">
```

**Regra:** todo `<form method="post">` deve usar `th:action="@{/rota}"` — nunca `action="/rota"`.

### `th:inline="javascript"` obrigatório em blocos `<script>`

Sem o atributo `th:inline="javascript"` na tag `<script>`, o Thymeleaf ignora completamente expressões inline `/*[[${variavel}]]*/` — o valor nunca é substituído e sempre chega como o comentário literal ou `false`.

```html
<!-- ERRADO (expressão ignorada silenciosamente): -->
<script>
  const modalAberto = /*[[${modalAberto}]]*/ false;
</script>

<!-- CORRETO: -->
<script th:inline="javascript">
  const modalAberto = /*[[${modalAberto}]]*/ false;
</script>
```

O bug é traiçoeiro porque **não gera erro** — o template renderiza normalmente mas a variável nunca recebe o valor do servidor.

**Regra:** todo `<script>` que usa expressões Thymeleaf precisa de `th:inline="javascript"`.

### UNIQUE constraint no PostgreSQL é case-sensitive

A constraint `UNIQUE` padrão do PostgreSQL não cobre variações de capitalização. `"João"` e `"JOÃO"` são valores distintos para o banco.

**Solução:** índice funcional com `LOWER()`:

```sql
ALTER TABLE reservas DROP CONSTRAINT IF EXISTS uq_reservas_nome;
CREATE UNIQUE INDEX IF NOT EXISTS uq_reservas_nome_ci ON reservas (LOWER(nome));
```

A validação no service usa `existsByNomeIgnoreCase()` que resolve no lado Java, mas o índice funcional garante a segunda linha de defesa também no banco.

### Validação de duplicidade em edição

Ao verificar nome duplicado na edição, usar `existsByNomeIgnoreCaseAndIdNot()` excluindo o próprio ID — caso contrário, editar sem mudar o nome sempre retorna falso positivo de duplicidade.

```java
// ERRADO para edição:
repository.existsByNomeIgnoreCase(nome)           // falha ao salvar sem alterar o nome

// CORRETO para edição:
repository.existsByNomeIgnoreCaseAndIdNot(nome, form.getId())
```

### Checkbox de toggle — `this.closest('form').submit()`

O submit do checkbox de pagamento usa `this.closest('form').submit()` em vez de `document.getElementById('form-toggle-X')`. Evita interpolação de ID no Thymeleaf e funciona corretamente com qualquer número de linhas na tabela.

---

## 10. Como fazer deploy na VPS

1. Copie `.env.example` para `.env` e preencha as senhas reais:

```bash
cp .env.example .env
# edite .env com senhas seguras — nunca commite este arquivo
```

2. Suba com Docker Compose:

```bash
docker compose up -d --build
```

O Compose constrói a imagem da aplicação (`Dockerfile` multi-stage: build com Maven sobre `eclipse-temurin:17-jdk-alpine` + runtime com `eclipse-temurin:17-jre-alpine`), aguarda o PostgreSQL passar no healthcheck e sobe a aplicação na porta definida em `APP_PORT` (padrão 8080). A JVM roda sob o usuário não-root `spring:spring` criado no Dockerfile.

Para acompanhar os logs:

```bash
docker compose logs -f app
```

---

## 11. Convenções do projeto

- **Idioma do domínio:** português para nomes de entidades, campos, métodos de serviço e variáveis de negócio (`reserva`, `pago`, `dataPagamento`, `listarTodas`, `togglePagamento`).
- **Idioma técnico:** inglês para padrões de framework (`findByPagoOrderByNomeAsc`, `@PrePersist`, `filterChain`, `userDetailsService`).
- **Lombok:** usar `@Data` + `@NoArgsConstructor` nas entidades; `@Data` em form DTOs (`ReservaForm`); `@Data` + `@AllArgsConstructor` em DTOs de projeção somente leitura (`ResumoDTO`, instanciado pelo service via construtor); `@RequiredArgsConstructor` nos controllers e services para injeção de dependência.
- **Templates:** Thymeleaf puro — sem frameworks JS, sem SPA. Toda lógica de renderização fica no servidor.
- **Sem API REST:** não há `@RestController` nem endpoints JSON. Tudo é renderizado server-side e retorna HTML.
- **Form objects:** controllers recebem DTOs (`ReservaForm`), nunca a entidade JPA diretamente.
- **Transações:** anotações `@Transactional` ficam no service, nunca no controller.
- **Flash messages:** usar `RedirectAttributes.addFlashAttribute("sucesso", ...)` após qualquer POST bem-sucedido.

### Dados do request HTTP nos templates

Nunca acessar `#httpServletRequest` diretamente nos templates Thymeleaf. Toda informação derivada do request (queryString, path, headers) deve ser calculada no controller e passada via `model.addAttribute()`.

**Motivo:** métodos do `HttpServletRequest` podem retornar `null` e o SpEL do Thymeleaf não tolera `null` em expressões de concatenação ou comparação — estoura `SpelEvaluationException` em tempo de renderização.

**Padrão adotado no projeto:**

```java
// Controller
String qs = request.getQueryString();
model.addAttribute("currentQuery", qs != null ? "?" + qs : "");
```

```html
<!-- Template -->
th:value="${'/' + currentQuery}"
```

---

## Banco de dados

### Seeds

- Scripts em `src/main/resources/db/`
- Montados no container via `/docker-entrypoint-initdb.d/` — o PostgreSQL executa automaticamente todos os `.sql` em ordem alfabética **apenas na primeira vez que o volume é criado** (banco ainda não existe)
- Controlados pela tabela `db_seeds` — cada seed tem um nome único e executa exatamente uma vez, independente do estado do volume
- **Para novos dados:** criar `seed_convidados_v2` (novo bloco `DO $$` com nome diferente), **nunca editar seeds anteriores**
- Nome do arquivo montado no container: `01_seed.sql` (prefixo numérico define ordem de execução)

**Como reaplicar o seed em dev** (quando o volume já existe e o `initdb` não roda):

```bash
docker exec -i baile-db-dev psql -U baile -d baile_namorados < src/main/resources/db/seed.sql
```

O bloco `DO $$` garante idempotência — rodar duas vezes não duplica dados.

---

## Memória do agente

A pasta `memory/` na raiz é criada e gerenciada automaticamente pelo Claude Code entre sessões.

**Estrutura:**
```
memory/MEMORY.md   → índice com links para cada arquivo de memória
memory/*.md        → registros individuais de decisões, correções e armadilhas
```

Esses arquivos não devem ser editados manualmente nem commitados com dados sensíveis.
Adicione `memory/` no `.gitignore` se não quiser versionar os registros do agente.

Se quiser versionar (recomendado para times), mantenha no Git — funciona como um diário de decisões técnicas do projeto acessível a qualquer desenvolvedor.
