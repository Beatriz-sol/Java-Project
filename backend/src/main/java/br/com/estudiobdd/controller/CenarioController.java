package br.com.estudiobdd.controller;

import br.com.estudiobdd.dto.CenarioDtos.CenarioRequest;
import br.com.estudiobdd.dto.CenarioDtos.CenarioResponse;
import br.com.estudiobdd.service.CenarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cenarios")
public class CenarioController {

    private final CenarioService cenarioService;

    public CenarioController(CenarioService cenarioService) {
        this.cenarioService = cenarioService;
    }

    @GetMapping("/{id}")
    public CenarioResponse buscar(@PathVariable Long id) {
        return CenarioResponse.de(cenarioService.buscar(id));
    }

    /** Substitui título, origem e passos (na ordem enviada). */
    @PutMapping("/{id}")
    public CenarioResponse atualizar(@PathVariable Long id,
                                     @RequestHeader(Cabecalhos.USUARIO) Long usuarioId,
                                     @Valid @RequestBody CenarioRequest req) {
        return CenarioResponse.de(cenarioService.atualizar(id, usuarioId, req.titulo(),
                req.origem(), req.passosDominio()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @RequestHeader(Cabecalhos.USUARIO) Long usuarioId) {
        cenarioService.excluir(id, usuarioId);
    }
}
