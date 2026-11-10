package br.com.estudiobdd.dto;

import br.com.estudiobdd.model.Cenario;
import br.com.estudiobdd.model.OrigemCenario;
import br.com.estudiobdd.model.Passo;
import br.com.estudiobdd.model.TipoPasso;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CenarioDtos {

    private CenarioDtos() {
    }

    public record PassoRequest(
            @NotNull TipoPasso tipo,
            @NotBlank @Size(max = 500) String texto) {

        public Passo paraDominio() {
            return new Passo(tipo, texto.trim());
        }
    }

    /** Os passos chegam na ordem desejada; a posição é a ordem da lista. */
    public record CenarioRequest(
            @NotBlank @Size(max = 200) String titulo,
            OrigemCenario origem,
            @NotNull List<@Valid PassoRequest> passos) {

        public List<Passo> passosDominio() {
            return passos.stream().map(PassoRequest::paraDominio).toList();
        }
    }

    public record PassoResponse(Long id, TipoPasso tipo, String palavraChave, String texto, int posicao) {
        public static PassoResponse de(Passo p) {
            return new PassoResponse(p.getId(), p.getTipo(), p.getTipo().getPalavraChave(),
                    p.getTexto(), p.getPosicao());
        }
    }

    public record CenarioResponse(Long id, Long historiaId, String titulo, int posicao,
                                  OrigemCenario origem, List<PassoResponse> passos) {
        public static CenarioResponse de(Cenario c) {
            return new CenarioResponse(c.getId(), c.getHistoria().getId(), c.getTitulo(), c.getPosicao(),
                    c.getOrigem(), c.getPassos().stream().map(PassoResponse::de).toList());
        }
    }
}
