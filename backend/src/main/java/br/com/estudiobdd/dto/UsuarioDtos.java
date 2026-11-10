package br.com.estudiobdd.dto;

import br.com.estudiobdd.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UsuarioDtos {

    private UsuarioDtos() {
    }

    public record UsuarioRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 160) String email) {
    }

    public record UsuarioResponse(Long id, String nome, String email) {
        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail());
        }
    }
}
