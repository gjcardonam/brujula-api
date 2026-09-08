package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.ComponenteEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponenteJpa extends JpaRepository<ComponenteEntidad, Long> {
    List<ComponenteEntidad> findAllByOrderByIdAsc();

    List<ComponenteEntidad> findByEstadoOrderByIdAsc(String estado);
}
