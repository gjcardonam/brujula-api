package co.edu.udea.brujula.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompetenciaRepository extends JpaRepository<Competencia, Long> {
    List<Competencia> findAllByOrderByIdAsc();
    List<Competencia> findByEstadoOrderByIdAsc(String estado);
}
