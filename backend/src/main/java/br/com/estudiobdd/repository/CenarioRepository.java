package br.com.estudiobdd.repository;

import br.com.estudiobdd.model.Cenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenarioRepository extends JpaRepository<Cenario, Long> {

    boolean existsByHistoriaIdAndTituloIgnoreCase(Long historiaId, String titulo);

    boolean existsByHistoriaIdAndTituloIgnoreCaseAndIdNot(Long historiaId, String titulo, Long id);
}
