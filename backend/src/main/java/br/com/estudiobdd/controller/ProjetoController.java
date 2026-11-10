package br.com.estudiobdd.controller;

import br.com.estudiobdd.dto.HistoriaDtos.HistoriaItemResponse;
import br.com.estudiobdd.dto.HistoriaDtos.HistoriaRequest;
import br.com.estudiobdd.dto.HistoriaDtos.HistoriaResponse;
import br.com.estudiobdd.dto.ProjetoDtos.ProjetoRequest;
import br.com.estudiobdd.dto.ProjetoDtos.ProjetoResponse;
import br.com.estudiobdd.model.Prioridade;
import br.com.estudiobdd.service.HistoriaService;
import br.com.estudiobdd.service.ProjetoService;
import br.com.estudiobdd.service.ResumoProjeto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final HistoriaService historiaService;

    public ProjetoController(ProjetoService projetoService, HistoriaService historiaService) {
        this.projetoService = projetoService;
        this.historiaService = historiaService;
    }

    @GetMapping
    public List<ProjetoResponse> listar() {
        return projetoService.listar().stream().map(ProjetoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ProjetoResponse buscar(@PathVariable Long id) {
        return ProjetoResponse.de(projetoService.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetoResponse criar(@Valid @RequestBody ProjetoRequest req) {
        return ProjetoResponse.de(projetoService.criar(req.nome(), req.descricao(), req.contextoDominio()));
    }

    @PutMapping("/{id}")
    public ProjetoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProjetoRequest req) {
        return ProjetoResponse.de(projetoService.atualizar(id, req.nome(), req.descricao(), req.contextoDominio()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        projetoService.excluir(id);
    }

    /** Regra 6: histórias com qtd. de cenários e de sugestões pendentes; filtro por prioridade. */
    @GetMapping("/{id}/historias")
    public List<HistoriaItemResponse> listarHistorias(@PathVariable Long id,
                                                      @RequestParam(required = false) Prioridade prioridade) {
        return historiaService.listarPorProjeto(id, prioridade).stream()
                .map(HistoriaItemResponse::de).toList();
    }

    @PostMapping("/{id}/historias")
    @ResponseStatus(HttpStatus.CREATED)
    public HistoriaResponse criarHistoria(@PathVariable Long id,
                                          @RequestHeader(Cabecalhos.USUARIO) Long usuarioId,
                                          @Valid @RequestBody HistoriaRequest req) {
        return HistoriaResponse.de(historiaService.criar(id, usuarioId, req.titulo(), req.papel(),
                req.objetivo(), req.beneficio(), req.prioridade()));
    }

    /** Regra 7. */
    @GetMapping("/{id}/resumo")
    public ResumoProjeto resumo(@PathVariable Long id) {
        return projetoService.resumo(id);
    }
}
