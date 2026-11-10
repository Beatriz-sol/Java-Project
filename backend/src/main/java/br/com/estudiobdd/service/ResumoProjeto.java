package br.com.estudiobdd.service;

import java.util.Map;

/** Regra 7: números do resumo do projeto. */
public record ResumoProjeto(
        Long projetoId,
        String projetoNome,
        long totalHistorias,
        Map<String, Long> cenariosPorOrigem,
        Map<String, Long> sugestoesPorDecisao,
        Map<String, Long> motivosRejeicao   // ordenado do mais para o menos frequente
) {
}
