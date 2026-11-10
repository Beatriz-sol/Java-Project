package br.com.estudiobdd.dto;

import br.com.estudiobdd.dto.CenarioDtos.CenarioResponse;
import br.com.estudiobdd.model.HistoriaUsuario;
import br.com.estudiobdd.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public final class HistoriaDtos {

    private HistoriaDtos() {
    }

    public record HistoriaRequest(
            @NotBlank @Size(max = 160) String titulo,
            @NotBlank(message = "informe o papel (Como...)") @Size(max = 200) String papel,
            @NotBlank(message = "informe o objetivo (Quero...)") @Size(max = 500) String objetivo,
            @NotBlank(message = "informe o benefício (Para...)") @Size(max = 500) String beneficio,
            Prioridade prioridade) {
    }

    /** Item da consulta do projeto (regra 6). */
    public record HistoriaItemResponse(Long id, String titulo, Prioridade prioridade,
                                       Long autorId, String autorNome,
                                       int qtdCenarios, long qtdSugestoesPendentes) {
        public static HistoriaItemResponse de(HistoriaUsuario h) {
            return new HistoriaItemResponse(h.getId(), h.getTitulo(), h.getPrioridade(),
                    h.getAutor().getId(), h.getAutor().getNome(),
                    h.getCenarios().size(), h.contarSugestoesPendentes());
        }
    }

    public record HistoriaResponse(Long id, Long projetoId, String projetoNome, String titulo,
                                   String papel, String objetivo, String beneficio,
                                   Prioridade prioridade, Long autorId, String autorNome,
                                   LocalDateTime criadaEm, LocalDateTime atualizadaEm,
                                   long qtdSugestoesPendentes, List<CenarioResponse> cenarios) {
        public static HistoriaResponse de(HistoriaUsuario h) {
            return new HistoriaResponse(h.getId(), h.getProjeto().getId(), h.getProjeto().getNome(),
                    h.getTitulo(), h.getPapel(), h.getObjetivo(), h.getBeneficio(), h.getPrioridade(),
                    h.getAutor().getId(), h.getAutor().getNome(), h.getCriadaEm(), h.getAtualizadaEm(),
                    h.contarSugestoesPendentes(),
                    h.getCenarios().stream().map(CenarioResponse::de).toList());
        }
    }
}
