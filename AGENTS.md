# AGENTS.md — regras para quem (pessoa ou IA) mexer no código

## Arquitetura (cinco camadas, nesta direção)
SPA (`frontend/`) → `controller` → `service` → `repository` → banco
`model` = entidades JPA e enums. `dto` = records de entrada/saída. `config` = CORS e dados iniciais.

- **Controller** recebe a requisição, valida o DTO (`@Valid`), converte DTO ↔ domínio e devolve a resposta.
  Nunca acessa `repository` (o teste ArchUnit quebra se acontecer).
- **Service** aplica TODAS as regras de negócio e controla as transações (`@Transactional`).
  Erros de regra viram `RegraNegocioException` (422), `ConflitoException` (409),
  `AcessoNegadoException` (403) ou `NaoEncontradoException` (404).
- **Repository** só interfaces Spring Data JPA, sem regra.
- **Model** não depende de nenhuma outra camada.
- **SPA** não tem regra de negócio: só mostra os dados e as mensagens da API.

## Convenções
- Dependências sempre pelo construtor (sem `@Autowired` em campo).
- Associações `@ManyToOne` com `fetch = LAZY`.
- Passos do cenário são enviados como lista completa; a posição é a ordem da lista (1, 2, 3...).
- Usuário atual: cabeçalho `X-Usuario-Id` (identidade simulada, não é autenticação).
- Nomes em português, sem acentos em identificadores Java.
- Toda nova regra ganha primeiro um cenário (Dado/Quando/Então) e depois um teste.

## Comandos
```bash
docker compose up -d                                   # PostgreSQL
cd backend && mvn spring-boot:run                      # API em http://localhost:8080
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=h2   # sem PostgreSQL (dados em memória)
cd backend && mvn test                                 # testes de regras + ArchUnit
cd frontend && python3 -m http.server 5173             # SPA em http://localhost:5173
```

## Erros que a IA já cometeu (atualizar sempre)
- (vazio por enquanto)
