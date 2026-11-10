package br.com.estudiobdd.service;

import br.com.estudiobdd.model.*;
import br.com.estudiobdd.repository.SugestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SugestaoService {

    private final SugestaoRepository sugestaoRepository;
    private final HistoriaService historiaService;
    private final UsuarioService usuarioService;

    public SugestaoService(SugestaoRepository sugestaoRepository,
                           HistoriaService historiaService,
                           UsuarioService usuarioService) {
        this.sugestaoRepository = sugestaoRepository;
        this.historiaService = historiaService;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<Sugestao> listar(Long historiaId, Decisao decisao) {
        historiaService.buscar(historiaId); // 404 se não existir
        return decisao == null
                ? sugestaoRepository.findByHistoriaIdOrderByRegistradaEmAsc(historiaId)
                : sugestaoRepository.findByHistoriaIdAndDecisaoOrderByRegistradaEmAsc(historiaId, decisao);
    }

    @Transactional(readOnly = true)
    public Sugestao buscar(Long id) {
        return sugestaoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Sugestão " + id + " não encontrada."));
    }

    /** Regra 4: toda sugestão nasce PENDENTE. */
    @Transactional
    public Sugestao registrar(Long historiaId, Long usuarioId, String ferramenta,
                              String prompt, String conteudo) {
        Usuario usuario = usuarioService.buscar(usuarioId);
        HistoriaUsuario historia = historiaService.buscar(historiaId);

        Sugestao sugestao = new Sugestao(ferramenta.trim(), prompt, conteudo, usuario);
        historia.adicionarSugestao(sugestao);
        return sugestaoRepository.save(sugestao);
    }

    /** Aceitar NÃO altera os cenários: o analista ajusta depois (regra 4). */
    @Transactional
    public Sugestao aceitar(Long id, Long usuarioId, String justificativa) {
        Sugestao sugestao = buscar(id);
        exigirPendente(sugestao);
        Usuario usuario = usuarioService.buscar(usuarioId);
        sugestao.aceitar(usuario, justificativa);
        return sugestao;
    }

    /** Regra 4: rejeitar exige motivo; OUTRO exige também a justificativa. */
    @Transactional
    public Sugestao rejeitar(Long id, Long usuarioId, MotivoRejeicao motivo, String justificativa) {
        Sugestao sugestao = buscar(id);
        exigirPendente(sugestao);
        if (motivo == null) {
            throw new RegraNegocioException("A rejeição exige um motivo "
                    + "(INCORRETA, GENERICA, DUPLICADA, FORA_DO_ESCOPO ou OUTRO).");
        }
        if (motivo == MotivoRejeicao.OUTRO && (justificativa == null || justificativa.isBlank())) {
            throw new RegraNegocioException("Com o motivo OUTRO, a justificativa é obrigatória.");
        }
        Usuario usuario = usuarioService.buscar(usuarioId);
        sugestao.rejeitar(usuario, motivo, justificativa);
        return sugestao;
    }

    /** Regra 4: sugestão já decidida não pode ser alterada. */
    private static void exigirPendente(Sugestao sugestao) {
        if (!sugestao.isPendente()) {
            throw new RegraNegocioException("Esta sugestão já foi decidida ("
                    + sugestao.getDecisao() + ") e não pode ser alterada.");
        }
    }
}
