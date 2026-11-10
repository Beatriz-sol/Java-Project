package br.com.estudiobdd.service;

import br.com.estudiobdd.model.*;
import br.com.estudiobdd.repository.HistoriaUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoriaService {

    private final HistoriaUsuarioRepository historiaRepository;
    private final ProjetoService projetoService;
    private final UsuarioService usuarioService;

    public HistoriaService(HistoriaUsuarioRepository historiaRepository,
                           ProjetoService projetoService,
                           UsuarioService usuarioService) {
        this.historiaRepository = historiaRepository;
        this.projetoService = projetoService;
        this.usuarioService = usuarioService;
    }

    /** Regra 6: histórias do projeto, com filtro opcional por prioridade. */
    @Transactional(readOnly = true)
    public List<HistoriaUsuario> listarPorProjeto(Long projetoId, Prioridade prioridade) {
        projetoService.buscar(projetoId); // 404 se o projeto não existir
        return prioridade == null
                ? historiaRepository.findByProjetoIdOrderByIdAsc(projetoId)
                : historiaRepository.findByProjetoIdAndPrioridadeOrderByIdAsc(projetoId, prioridade);
    }

    @Transactional(readOnly = true)
    public HistoriaUsuario buscar(Long id) {
        return historiaRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("História " + id + " não encontrada."));
    }

    /** Regra 3: carrega a história e garante que o usuário atual é o autor. */
    @Transactional(readOnly = true)
    public HistoriaUsuario buscarComoAutor(Long id, Long usuarioId) {
        usuarioService.buscar(usuarioId);
        HistoriaUsuario historia = buscar(id);
        if (!historia.isAutor(usuarioId)) {
            throw new AcessoNegadoException(
                    "Somente o autor (" + historia.getAutor().getNome()
                            + ") pode editar ou excluir esta história e seus cenários.");
        }
        return historia;
    }

    @Transactional
    public HistoriaUsuario criar(Long projetoId, Long usuarioId, String titulo, String papel,
                                 String objetivo, String beneficio, Prioridade prioridade) {
        Usuario autor = usuarioService.buscar(usuarioId);
        Projeto projeto = projetoService.buscar(projetoId);
        validarNarrativa(titulo, papel, objetivo, beneficio);

        if (historiaRepository.existsByProjetoIdAndTituloIgnoreCase(projetoId, titulo.trim())) {
            throw new ConflitoException("Já existe a história \"" + titulo.trim() + "\" neste projeto.");
        }

        HistoriaUsuario historia = new HistoriaUsuario(projeto, autor, titulo.trim(), papel.trim(),
                objetivo.trim(), beneficio.trim(), prioridade == null ? Prioridade.MEDIA : prioridade);
        return historiaRepository.save(historia);
    }

    @Transactional
    public HistoriaUsuario atualizar(Long id, Long usuarioId, String titulo, String papel,
                                     String objetivo, String beneficio, Prioridade prioridade) {
        HistoriaUsuario historia = buscarComoAutor(id, usuarioId);
        validarNarrativa(titulo, papel, objetivo, beneficio);

        Long projetoId = historia.getProjeto().getId();
        if (historiaRepository.existsByProjetoIdAndTituloIgnoreCaseAndIdNot(projetoId, titulo.trim(), id)) {
            throw new ConflitoException("Já existe a história \"" + titulo.trim() + "\" neste projeto.");
        }

        historia.setTitulo(titulo.trim());
        historia.setPapel(papel.trim());
        historia.setObjetivo(objetivo.trim());
        historia.setBeneficio(beneficio.trim());
        if (prioridade != null) {
            historia.setPrioridade(prioridade);
        }
        return historia;
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        HistoriaUsuario historia = buscarComoAutor(id, usuarioId);
        historia.getProjeto().getHistorias().remove(historia); // orphanRemoval apaga
        historiaRepository.delete(historia);
    }

    /** Regra 5: exporta em Gherkin (pt) se houver cenários e nenhuma sugestão pendente. */
    @Transactional(readOnly = true)
    public String exportarFeature(Long id) {
        HistoriaUsuario historia = buscar(id);

        if (historia.getCenarios().isEmpty()) {
            throw new RegraNegocioException("Exportação bloqueada: a história não tem cenários.");
        }
        long pendentes = historia.contarSugestoesPendentes();
        if (pendentes > 0) {
            throw new RegraNegocioException("Exportação bloqueada: há " + pendentes
                    + " sugestão(ões) pendente(s). Aceite ou rejeite antes de exportar.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("# language: pt\n");
        sb.append("Funcionalidade: ").append(historia.getTitulo()).append('\n');
        sb.append("  Como ").append(historia.getPapel()).append('\n');
        sb.append("  Quero ").append(historia.getObjetivo()).append('\n');
        sb.append("  Para ").append(historia.getBeneficio()).append('\n');

        for (Cenario cenario : historia.getCenarios()) {
            sb.append('\n');
            sb.append("  Cenário: ").append(cenario.getTitulo()).append('\n');
            for (Passo passo : cenario.getPassos()) {
                sb.append("    ").append(passo.getTipo().getPalavraChave())
                        .append(' ').append(passo.getTexto()).append('\n');
            }
        }
        return sb.toString();
    }

    /** Regra 1: papel, objetivo e benefício obrigatórios. */
    private static void validarNarrativa(String titulo, String papel, String objetivo, String beneficio) {
        if (vazio(titulo)) throw new RegraNegocioException("O título da história é obrigatório.");
        if (vazio(papel)) throw new RegraNegocioException("Narrativa incompleta: informe o papel (Como...).");
        if (vazio(objetivo)) throw new RegraNegocioException("Narrativa incompleta: informe o objetivo (Quero...).");
        if (vazio(beneficio)) throw new RegraNegocioException("Narrativa incompleta: informe o benefício (Para...).");
    }

    private static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}
