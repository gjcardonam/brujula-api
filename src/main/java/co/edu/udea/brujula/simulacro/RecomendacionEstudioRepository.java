package co.edu.udea.brujula.simulacro;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecomendacionEstudioRepository extends JpaRepository<RecomendacionEstudio, Long> {
    @EntityGraph(attributePaths = {"componente", "competencia"})
    List<RecomendacionEstudio> findBySimulacro_IdOrderByOrdenPrioridadAsc(Long idSimulacro);
}
