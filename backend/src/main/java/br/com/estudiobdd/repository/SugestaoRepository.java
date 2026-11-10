package br.com.estudiobdd.repository;

import br.com.estudiobdd.model.Decisao;
import br.com.estudiobdd.model.Sugestao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SugestaoRepository extends JpaRepository<Sugestao, Long> {

    List<Sugestao> findByHistoriaIdOrderByRegistradaEmAsc(Long historiaId);

    List<Sugestao> findByHistoriaIdAndDecisaoOrderByRegistradaEmAsc(Long historiaId, Decisao decisao);
}
