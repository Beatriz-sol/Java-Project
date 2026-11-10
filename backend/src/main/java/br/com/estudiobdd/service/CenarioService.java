package br.com.estudiobdd.service;

import br.com.estudiobdd.model.Cenario;
import br.com.estudiobdd.model.HistoriaUsuario;
import br.com.estudiobdd.model.OrigemCenario;
import br.com.estudiobdd.model.Passo;
import br.com.estudiobdd.repository.CenarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CenarioService {

    private final CenarioRepository cenarioRepository;
    private final HistoriaService historiaService;

    public CenarioService(CenarioRepository cenarioRepository, HistoriaService historiaService) {
        this.cenarioRepository = cenarioRepository;
        this.historiaService = historiaService;
    }

    @Transactional(readOnly = true)
    public List<Cenario> listarPorHistoria(Long historiaId) {
        return historiaService.buscar(historiaId).getCenarios();
    }

    @Transactional(readOnly = true)
    public Cenario buscar(Long id) {
        return cenarioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Cenário " + id + " não encontrado."));
    }

    /**
     * Caminho da operação "cadastrar um cenário":
     * controller (valida DTO) -> aqui (regras 2 e 3) -> repositório -> banco.
     */
    @Transactional
    public Cenario criar(Long historiaId, Long usuarioId, String titulo,
                         OrigemCenario origem, List<Passo> passos) {
        HistoriaUsuario historia = historiaService.buscarComoAutor(historiaId, usuarioId); // regra 3
        validarTitulo(titulo);
        ValidadorCenario.validarPassos(passos.stream().map(Passo::getTipo).toList()); // regra 2

        if (cenarioRepository.existsByHistoriaIdAndTituloIgnoreCase(historiaId, titulo.trim())) {
            throw new ConflitoException("Já existe o cenário \"" + titulo.trim() + "\" nesta história.");
        }

        Cenario cenario = new Cenario(titulo.trim(), origem);
        cenario.substituirPassos(passos);
        historia.adicionarCenario(cenario);
        return cenarioRepository.save(cenario);
    }

    /** Edição completa: título, origem e a lista de passos na nova ordem. */
    @Transactional
    public Cenario atualizar(Long id, Long usuarioId, String titulo,
                             OrigemCenario origem, List<Passo> passos) {
        Cenario cenario = buscar(id);
        Long historiaId = cenario.getHistoria().getId();
        historiaService.buscarComoAutor(historiaId, usuarioId); // regra 3
        validarTitulo(titulo);
        ValidadorCenario.validarPassos(passos.stream().map(Passo::getTipo).toList()); // regra 2

        if (cenarioRepository.existsByHistoriaIdAndTituloIgnoreCaseAndIdNot(historiaId, titulo.trim(), id)) {
            throw new ConflitoException("Já existe o cenário \"" + titulo.trim() + "\" nesta história.");
        }

        cenario.setTitulo(titulo.trim());
        if (origem != null) {
            cenario.setOrigem(origem);
        }
        cenario.substituirPassos(passos);
        return cenario;
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Cenario cenario = buscar(id);
        HistoriaUsuario historia = historiaService.buscarComoAutor(cenario.getHistoria().getId(), usuarioId);
        historia.getCenarios().remove(cenario); // orphanRemoval apaga o cenário e seus passos
    }

    private static void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new RegraNegocioException("O título do cenário é obrigatório.");
        }
    }
}
