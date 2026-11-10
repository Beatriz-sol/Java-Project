package br.com.estudiobdd.repository;

import br.com.estudiobdd.model.HistoriaUsuario;
import br.com.estudiobdd.model.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriaUsuarioRepository extends JpaRepository<HistoriaUsuario, Long> {

    List<HistoriaUsuario> findByProjetoIdOrderByIdAsc(Long projetoId);

    List<HistoriaUsuario> findByProjetoIdAndPrioridadeOrderByIdAsc(Long projetoId, Prioridade prioridade);

    boolean existsByProjetoIdAndTituloIgnoreCase(Long projetoId, String titulo);

    boolean existsByProjetoIdAndTituloIgnoreCaseAndIdNot(Long projetoId, String titulo, Long id);
}
