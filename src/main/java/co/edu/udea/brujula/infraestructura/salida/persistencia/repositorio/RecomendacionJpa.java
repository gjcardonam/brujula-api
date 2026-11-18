package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.RecomendacionEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecomendacionJpa extends JpaRepository<RecomendacionEntidad, Long> {
    List<RecomendacionEntidad> findByIdSimulacroOrderByOrdenAsc(Long idSimulacro);
}
