package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.CompetenciaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetenciaJpa extends JpaRepository<CompetenciaEntidad, Long> {
    List<CompetenciaEntidad> findAllByOrderByIdAsc();

    List<CompetenciaEntidad> findByEstadoOrderByIdAsc(String estado);
}
