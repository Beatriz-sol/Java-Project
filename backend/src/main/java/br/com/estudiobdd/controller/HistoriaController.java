package br.com.estudiobdd.controller;

import br.com.estudiobdd.dto.CenarioDtos.CenarioRequest;
import br.com.estudiobdd.dto.CenarioDtos.CenarioResponse;
import br.com.estudiobdd.dto.HistoriaDtos.HistoriaRequest;
import br.com.estudiobdd.dto.HistoriaDtos.HistoriaResponse;
import br.com.estudiobdd.dto.SugestaoDtos.SugestaoRequest;
import br.com.estudiobdd.dto.SugestaoDtos.SugestaoResponse;
import br.com.estudiobdd.model.Decisao;
import br.com.estudiobdd.model.HistoriaUsuario;
import br.com.estudiobdd.service.CenarioService;
import br.com.estudiobdd.service.HistoriaService;
import br.com.estudiobdd.service.SugestaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;

@RestController
@RequestMapping("/api/historias")
public class HistoriaController {

    private final HistoriaService historiaService;
    private final CenarioService cenarioService;
    private final SugestaoService sugestaoService;

    public HistoriaController(HistoriaService historiaService,
                              CenarioService cenarioService,
                              SugestaoService sugestaoService) {
        this.historiaService = historiaService;
        this.cenarioService = cenarioService;
        this.sugestaoService = sugestaoService;
    }

    @GetMapping("/{id}")
    public HistoriaResponse buscar(@PathVariable Long id) {
        return HistoriaResponse.de(historiaService.buscar(id));
    }

    @PutMapping("/{id}")
    public HistoriaResponse atualizar(@PathVariable Long id,
                                      @RequestHeader(Cabecalhos.USUARIO) Long usuarioId,
                                      @Valid @RequestBody HistoriaRequest req) {
        return HistoriaResponse.de(historiaService.atualizar(id, usuarioId, req.titulo(), req.papel(),
                req.objetivo(), req.beneficio(), req.prioridade()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id, @RequestHeader(Cabecalhos.USUARIO) Long usuarioId) {
        historiaService.excluir(id, usuarioId);
    }

    // ---------- Cenários da história ----------

    @GetMapping("/{id}/cenarios")
    public List<CenarioResponse> listarCenarios(@PathVariable Long id) {
        return cenarioService.listarPorHistoria(id).stream().map(CenarioResponse::de).toList();
    }

    @PostMapping("/{id}/cenarios")
    @ResponseStatus(HttpStatus.CREATED)
    public CenarioResponse criarCenario(@PathVariable Long id,
                                        @RequestHeader(Cabecalhos.USUARIO) Long usuarioId,
                                        @Valid @RequestBody CenarioRequest req) {
        return CenarioResponse.de(cenarioService.criar(id, usuarioId, req.titulo(),
                req.origem(), req.passosDominio()));
    }

    // ---------- Sugestões da história ----------

    @GetMapping("/{id}/sugestoes")
    public List<SugestaoResponse> listarSugestoes(@PathVariable Long id,
                                                  @RequestParam(required = false) Decisao decisao) {
        return sugestaoService.listar(id, decisao).stream().map(SugestaoResponse::de).toList();
    }

    @PostMapping("/{id}/sugestoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SugestaoResponse registrarSugestao(@PathVariable Long id,
                                              @RequestHeader(Cabecalhos.USUARIO) Long usuarioId,
                                              @Valid @RequestBody SugestaoRequest req) {
        return SugestaoResponse.de(sugestaoService.registrar(id, usuarioId,
                req.ferramenta(), req.prompt(), req.conteudo()));
    }

    // ---------- Exportação (regra 5) ----------

    @GetMapping("/{id}/feature")
    public ResponseEntity<String> exportar(@PathVariable Long id) {
        String conteudo = historiaService.exportarFeature(id);
        HistoriaUsuario historia = historiaService.buscar(id);
        String arquivo = nomeArquivo(historia.getTitulo()) + ".feature";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo + "\"")
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(conteudo);
    }

    private static String nomeArquivo(String titulo) {
        String semAcento = Normalizer.normalize(titulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = semAcento.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return slug.isEmpty() ? "historia" : slug;
    }
}
