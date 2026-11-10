package br.com.estudiobdd.service;

import br.com.estudiobdd.model.TipoPasso;

import java.util.List;

/**
 * Regra 2: a gramática do Gherkin.
 * - ao menos um passo Quando e um passo Então;
 * - ordem Dado -> Quando -> Então (nunca volta para trás);
 * - E e Mas continuam o tipo anterior e não podem iniciar o cenário.
 */
public final class ValidadorCenario {

    private ValidadorCenario() {
    }

    public static void validarPassos(List<TipoPasso> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            throw new RegraNegocioException("O cenário precisa ter passos (ao menos um Quando e um Então).");
        }

        TipoPasso tipoAtual = null;   // tipo "efetivo" (E/Mas herdam o anterior)
        boolean temQuando = false;
        boolean temEntao = false;

        for (int i = 0; i < tipos.size(); i++) {
            TipoPasso tipo = tipos.get(i);
            if (tipo == null) {
                throw new RegraNegocioException("O passo " + (i + 1) + " está sem tipo.");
            }

            if (tipo.isContinuacao()) {
                if (tipoAtual == null) {
                    throw new RegraNegocioException(
                            "O cenário não pode começar com " + tipo.getPalavraChave()
                                    + ". E/Mas apenas continuam o passo anterior.");
                }
                continue; // mantém o tipo efetivo anterior
            }

            if (tipoAtual != null && ordem(tipo) < ordem(tipoAtual)) {
                throw new RegraNegocioException(
                        "Passo " + (i + 1) + " fora de ordem: " + tipo.getPalavraChave()
                                + " não pode vir depois de " + tipoAtual.getPalavraChave()
                                + ". A ordem é Dado -> Quando -> Então.");
            }

            tipoAtual = tipo;
            if (tipo == TipoPasso.QUANDO) temQuando = true;
            if (tipo == TipoPasso.ENTAO) temEntao = true;
        }

        if (!temQuando) {
            throw new RegraNegocioException("O cenário precisa ter ao menos um passo Quando.");
        }
        if (!temEntao) {
            throw new RegraNegocioException("O cenário precisa ter ao menos um passo Então.");
        }
    }

    private static int ordem(TipoPasso tipo) {
        return switch (tipo) {
            case DADO -> 0;
            case QUANDO -> 1;
            case ENTAO -> 2;
            default -> throw new IllegalArgumentException("Tipo sem ordem própria: " + tipo);
        };
    }
}
