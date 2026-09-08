package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.NivelDificultadEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NivelDificultadJpa extends JpaRepository<NivelDificultadEntidad, Long> {
    List<NivelDificultadEntidad> findAllByOrderByIdAsc();
}
