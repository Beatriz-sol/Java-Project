package br.com.estudiobdd.dto;

import br.com.estudiobdd.model.Decisao;
import br.com.estudiobdd.model.MotivoRejeicao;
import br.com.estudiobdd.model.Sugestao;
import br.com.estudiobdd.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class SugestaoDtos {

    private SugestaoDtos() {
    }

    public record SugestaoRequest(
            @NotBlank @Size(max = 80) String ferramenta,
            @NotBlank String prompt,
            @NotBlank String conteudo) {
    }

    /**
     * Corpo de aceitar/rejeitar. O motivo não tem @NotNull de propósito:
     * a regra 4 é verificada no serviço, com mensagem própria.
     */
    public record DecisaoRequest(MotivoRejeicao motivo, @Size(max = 1000) String justificativa) {
    }

    public record SugestaoResponse(Long id, Long historiaId, String ferramenta, String prompt,
                                   String conteudo, Decisao decisao, MotivoRejeicao motivoRejeicao,
                                   String justificativa, LocalDateTime registradaEm,
                                   LocalDateTime decididaEm, String registradaPor, String decididaPor) {
        public static SugestaoResponse de(Sugestao s) {
            return new SugestaoResponse(s.getId(), s.getHistoria().getId(), s.getFerramenta(),
                    s.getPrompt(), s.getConteudo(), s.getDecisao(), s.getMotivoRejeicao(),
                    s.getJustificativa(), s.getRegistradaEm(), s.getDecididaEm(),
                    nome(s.getRegistradaPor()), nome(s.getDecididaPor()));
        }

        private static String nome(Usuario u) {
            return u == null ? null : u.getNome();
        }
    }
}
