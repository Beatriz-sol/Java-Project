package br.com.estudiobdd;

import br.com.estudiobdd.model.TipoPasso;
import br.com.estudiobdd.service.RegraNegocioException;
import br.com.estudiobdd.service.ValidadorCenario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.estudiobdd.model.TipoPasso.*;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Casos da regra 2 (gramática do Gherkin). */
class ValidadorCenarioTest {

    private static void valido(TipoPasso... tipos) {
        assertThatCode(() -> ValidadorCenario.validarPassos(List.of(tipos))).doesNotThrowAnyException();
    }

    private static void invalido(String trechoDaMensagem, TipoPasso... tipos) {
        assertThatThrownBy(() -> ValidadorCenario.validarPassos(List.of(tipos)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining(trechoDaMensagem);
    }

    @Test
    void aceitaCenariosValidos() {
        valido(DADO, QUANDO, ENTAO);
        valido(QUANDO, ENTAO);                       // Dado é opcional
        valido(DADO, E, QUANDO, ENTAO, E, MAS);      // E/Mas continuam o anterior
        valido(DADO, QUANDO, E, ENTAO);
    }

    @Test
    void rejeitaSemQuandoOuSemEntao() {
        invalido("Quando", DADO, ENTAO);
        invalido("Então", DADO, QUANDO);
        invalido("passos");                          // lista vazia
    }

    @Test
    void rejeitaForaDeOrdem() {
        invalido("fora de ordem", QUANDO, DADO, ENTAO);
        invalido("fora de ordem", DADO, QUANDO, ENTAO, QUANDO);
    }

    @Test
    void rejeitaInicioComEOuMas() {
        invalido("começar com E", E, QUANDO, ENTAO);
        invalido("começar com Mas", MAS, QUANDO, ENTAO);
    }
}
