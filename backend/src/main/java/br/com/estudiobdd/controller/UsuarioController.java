package br.com.estudiobdd.controller;

import br.com.estudiobdd.dto.UsuarioDtos.UsuarioRequest;
import br.com.estudiobdd.dto.UsuarioDtos.UsuarioResponse;
import br.com.estudiobdd.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar().stream().map(UsuarioResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest req) {
        return UsuarioResponse.de(usuarioService.criar(req.nome(), req.email()));
    }
}
