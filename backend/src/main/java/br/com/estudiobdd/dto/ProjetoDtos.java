package br.com.estudiobdd.dto;

import br.com.estudiobdd.model.Projeto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ProjetoDtos {

    private ProjetoDtos() {
    }

    public record ProjetoRequest(
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 500) String descricao,
            String contextoDominio) {
    }

    public record ProjetoResponse(Long id, String nome, String descricao,
                                  String contextoDominio, LocalDateTime dataCriacao) {
        public static ProjetoResponse de(Projeto p) {
            return new ProjetoResponse(p.getId(), p.getNome(), p.getDescricao(),
                    p.getContextoDominio(), p.getDataCriacao());
        }
    }
}
