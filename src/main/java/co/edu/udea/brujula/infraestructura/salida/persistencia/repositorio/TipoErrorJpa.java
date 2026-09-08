package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.TipoErrorEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoErrorJpa extends JpaRepository<TipoErrorEntidad, Long> {
    Optional<TipoErrorEntidad> findByNombre(String nombre);

    List<TipoErrorEntidad> findAllByOrderByIdAsc();
}
