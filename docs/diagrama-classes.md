# Diagrama de classes

```mermaid
classDiagram
    class Usuario {
        Long id
        String nome
        String email «único»
    }
    class Projeto {
        Long id
        String nome «único»
        String descricao
        String contextoDominio
        LocalDateTime dataCriacao
    }
    class HistoriaUsuario {
        Long id
        String titulo «único no projeto»
        String papel
        String objetivo
        String beneficio
        Prioridade prioridade
        LocalDateTime criadaEm
        LocalDateTime atualizadaEm
    }
    class Cenario {
        Long id
        String titulo «único na história»
        int posicao
        OrigemCenario origem
    }
    class Passo {
        Long id
        TipoPasso tipo
        String texto
        int posicao
    }
    class Sugestao {
        Long id
        String ferramenta
        String prompt
        String conteudo
        Decisao decisao
        MotivoRejeicao motivoRejeicao
        String justificativa
        LocalDateTime registradaEm
        LocalDateTime decididaEm
    }

    Projeto "1" --> "*" HistoriaUsuario : historias
    HistoriaUsuario "*" --> "1" Usuario : autor
    HistoriaUsuario "1" --> "*" Cenario : cenarios (ordenados)
    Cenario "1" *-- "*" Passo : passos (composição, ordenados)
    HistoriaUsuario "1" --> "*" Sugestao : sugestoes
    Sugestao "*" --> "1" Usuario : registradaPor
    Sugestao "*" --> "0..1" Usuario : decididaPor
```

Enums: `Prioridade` (ALTA, MEDIA, BAIXA), `OrigemCenario` (HUMANO, SUGESTAO_IA),
`TipoPasso` (DADO, QUANDO, ENTAO, E, MAS), `Decisao` (PENDENTE, ACEITA, REJEITADA),
`MotivoRejeicao` (INCORRETA, GENERICA, DUPLICADA, FORA_DO_ESCOPO, OUTRO).
