package br.com.estudiobdd.service;

import br.com.estudiobdd.model.*;
import br.com.estudiobdd.repository.ProjetoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    @Transactional(readOnly = true)
    public List<Projeto> listar() {
        return projetoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Projeto buscar(Long id) {
        return projetoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Projeto " + id + " não encontrado."));
    }

    @Transactional
    public Projeto criar(String nome, String descricao, String contextoDominio) {
        if (projetoRepository.existsByNomeIgnoreCase(nome.trim())) {
            throw new ConflitoException("Já existe um projeto chamado \"" + nome.trim() + "\".");
        }
        return projetoRepository.save(new Projeto(nome.trim(), descricao, contextoDominio));
    }

    @Transactional
    public Projeto atualizar(Long id, String nome, String descricao, String contextoDominio) {
        Projeto projeto = buscar(id);
        if (projetoRepository.existsByNomeIgnoreCaseAndIdNot(nome.trim(), id)) {
            throw new ConflitoException("Já existe um projeto chamado \"" + nome.trim() + "\".");
        }
        projeto.setNome(nome.trim());
        projeto.setDescricao(descricao);
        projeto.setContextoDominio(contextoDominio);
        return projeto;
    }

    @Transactional
    public void excluir(Long id) {
        projetoRepository.delete(buscar(id));
    }

    /** Regra 7. */
    @Transactional(readOnly = true)
    public ResumoProjeto resumo(Long id) {
        Projeto projeto = buscar(id);
        List<HistoriaUsuario> historias = projeto.getHistorias();

        Map<OrigemCenario, Long> porOrigem = new EnumMap<>(OrigemCenario.class);
        for (OrigemCenario o : OrigemCenario.values()) porOrigem.put(o, 0L);

        Map<Decisao, Long> porDecisao = new EnumMap<>(Decisao.class);
        for (Decisao d : Decisao.values()) porDecisao.put(d, 0L);

        Map<MotivoRejeicao, Long> porMotivo = new EnumMap<>(MotivoRejeicao.class);

        for (HistoriaUsuario historia : historias) {
            for (Cenario cenario : historia.getCenarios()) {
                porOrigem.merge(cenario.getOrigem(), 1L, Long::sum);
            }
            for (Sugestao sugestao : historia.getSugestoes()) {
                porDecisao.merge(sugestao.getDecisao(), 1L, Long::sum);
                if (sugestao.getMotivoRejeicao() != null) {
                    porMotivo.merge(sugestao.getMotivoRejeicao(), 1L, Long::sum);
                }
            }
        }

        Map<String, Long> motivosOrdenados = new LinkedHashMap<>();
        porMotivo.entrySet().stream()
                .sorted(Map.Entry.<MotivoRejeicao, Long>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> motivosOrdenados.put(e.getKey().name(), e.getValue()));

        return new ResumoProjeto(
                projeto.getId(),
                projeto.getNome(),
                historias.size(),
                comChavesTexto(porOrigem),
                comChavesTexto(porDecisao),
                motivosOrdenados);
    }

    private static <E extends Enum<E>> Map<String, Long> comChavesTexto(Map<E, Long> mapa) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        mapa.forEach((chave, valor) -> resultado.put(chave.name(), valor));
        return resultado;
    }
}
