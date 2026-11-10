# Estúdio BDD — documentação de requisitos com apoio de IA

Programação de Sistemas II · Projeto 2026/2 · **versão 0.1 (primeira versão para acompanhamento)**


Aplicação para registrar histórias de usuário e seus cenários BDD (Dado/Quando/Então),
registrar sugestões de ferramentas de IA (aceitas ou rejeitadas com motivo) e exportar
as histórias como arquivos `.feature`.

- `backend/` — API REST (Spring Boot 3, Spring Data JPA, PostgreSQL, Java 17+)
- `frontend/` — SPA em Vue 3 (sem build: basta servir a pasta)
- `docs/` — diagrama de classes e registro de IA

## Como executar

**Pré-requisitos:** Java 17+, Maven, Docker (para o PostgreSQL) e Python 3 (só para servir a SPA).

```bash
# 1. Banco
docker compose up -d

# 2. API (http://localhost:8080)
cd backend
mvn spring-boot:run

# 3. SPA (http://localhost:5173) — em outro terminal
cd frontend
python3 -m http.server 5173
```

Sem Docker/PostgreSQL à mão, a API também sobe com banco em memória:
`mvn spring-boot:run -Dspring-boot.run.profiles=h2` (os dados somem ao reiniciar).

Na primeira execução são carregados **dados fictícios**: usuários Ana, Bruno e Carla,
o projeto "Estúdio BDD" e três histórias (uma pronta para exportar, uma bloqueada por
sugestão pendente e uma de outro autor, para testar a regra de autoria).

**Testes:** `cd backend && mvn test` (três testes de regras obrigatórios, testes da regra 2 e ArchUnit).

## Identidade simulada (limitação conhecida)

O usuário atual é enviado no cabeçalho `X-Usuario-Id`. **Isso não é autenticação**:
qualquer cliente pode enviar qualquer id. Serve apenas para os serviços validarem a autoria (regra 3).

## Arquitetura

SPA → HTTP/JSON → `controller` → `service` → `repository` → PostgreSQL.
Entidades em `model`, DTOs em `dto`, CORS e dados iniciais em `config`.
Detalhes e convenções em [AGENTS.md](AGENTS.md).

Caminho de "cadastrar um cenário": `HistoriaController.criarCenario` valida o `CenarioRequest`
e converte os passos para `Passo` → `CenarioService.criar` verifica autoria (regra 3), título
único e a gramática dos passos com `ValidadorCenario` (regra 2) → `CenarioRepository.save`
grava o cenário e, em cascata, os passos.

Carregamento: todas as associações `@ManyToOne` são `LAZY`, e `@OneToMany` é `LAZY` por padrão.
Nesta versão o `open-in-view` está ligado para os controllers montarem os DTOs; é um ponto a revisar.

## Endpoints

Erros seguem o formato `ProblemDetail` (`application/problem+json`): 400 dados inválidos,
403 não é o autor, 404 não encontrado, 409 duplicado, 422 regra de negócio violada.
`*` = exige o cabeçalho `X-Usuario-Id`.

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/usuarios` | Lista usuários |
| POST | `/api/usuarios` | Cria usuário `{nome, email}` |
| GET | `/api/projetos` | Lista projetos |
| POST | `/api/projetos` | Cria projeto `{nome, descricao, contextoDominio}` |
| GET / PUT / DELETE | `/api/projetos/{id}` | Consulta / edita / exclui projeto |
| GET | `/api/projetos/{id}/historias?prioridade=ALTA` | Consulta do projeto (regra 6): histórias com nº de cenários e de sugestões pendentes |
| POST* | `/api/projetos/{id}/historias` | Cria história `{titulo, papel, objetivo, beneficio, prioridade}` |
| GET | `/api/projetos/{id}/resumo` | Resumo (regra 7) |
| GET | `/api/historias/{id}` | História com cenários e passos |
| PUT* / DELETE* | `/api/historias/{id}` | Edita / exclui (só o autor) |
| GET | `/api/historias/{id}/cenarios` | Cenários da história |
| POST* | `/api/historias/{id}/cenarios` | Cria cenário `{titulo, origem, passos:[{tipo, texto}]}` |
| GET | `/api/cenarios/{id}` | Consulta cenário |
| PUT* / DELETE* | `/api/cenarios/{id}` | Edita (substitui os passos, na ordem enviada) / exclui |
| GET | `/api/historias/{id}/sugestoes?decisao=PENDENTE` | Sugestões da história, por decisão |
| POST* | `/api/historias/{id}/sugestoes` | Registra sugestão `{ferramenta, prompt, conteudo}` (nasce PENDENTE) |
| GET | `/api/sugestoes/{id}` | Consulta sugestão |
| POST* | `/api/sugestoes/{id}/aceitar` | Aceita `{justificativa?}` |
| POST* | `/api/sugestoes/{id}/rejeitar` | Rejeita `{motivo, justificativa?}` |
| GET | `/api/historias/{id}/feature` | Exporta `.feature` (regra 5) |

Exemplo:

```bash
curl -X POST http://localhost:8080/api/historias/1/cenarios \
  -H "Content-Type: application/json" -H "X-Usuario-Id: 1" \
  -d '{"titulo":"Cenário sem Então","passos":[{"tipo":"DADO","texto":"x"},{"tipo":"QUANDO","texto":"y"}]}'
# 422: "O cenário precisa ter ao menos um passo Então."
```

## Situação desta versão

Feito: entidades e tabelas, CRUD de projetos/histórias/cenários/passos, regras 1 a 7,
sugestões, exportação, consulta e resumo, erros padronizados, identidade simulada,
testes de regras + ArchUnit, SPA com todas as telas mínimas e CORS restrito à SPA.

Próximos passos: roteiro de verificação com evidências (seção 13), documentação OpenAPI,
mais interações no registro de IA, revisar `open-in-view`, atividade bônus (se sobrar tempo).
