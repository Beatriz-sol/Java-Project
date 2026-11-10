package br.com.estudiobdd.controller;

import br.com.estudiobdd.dto.SugestaoDtos.DecisaoRequest;
import br.com.estudiobdd.dto.SugestaoDtos.SugestaoResponse;
import br.com.estudiobdd.service.SugestaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sugestoes")
public class SugestaoController {

    private final SugestaoService sugestaoService;

    public SugestaoController(SugestaoService sugestaoService) {
        this.sugestaoService = sugestaoService;
    }

    @GetMapping("/{id}")
    public SugestaoResponse buscar(@PathVariable Long id) {
        return SugestaoResponse.de(sugestaoService.buscar(id));
    }

    @PostMapping("/{id}/aceitar")
    public SugestaoResponse aceitar(@PathVariable Long id,
                                    @RequestHeader(Cabecalhos.USUARIO) Long usuarioId,
                                    @Valid @RequestBody(required = false) DecisaoRequest req) {
        String justificativa = req == null ? null : req.justificativa();
        return SugestaoResponse.de(sugestaoService.aceitar(id, usuarioId, justificativa));
    }

    @PostMapping("/{id}/rejeitar")
    public SugestaoResponse rejeitar(@PathVariable Long id,
                                     @RequestHeader(Cabecalhos.USUARIO) Long usuarioId,
                                     @Valid @RequestBody(required = false) DecisaoRequest req) {
        return SugestaoResponse.de(sugestaoService.rejeitar(id, usuarioId,
                req == null ? null : req.motivo(),
                req == null ? null : req.justificativa()));
    }
}
