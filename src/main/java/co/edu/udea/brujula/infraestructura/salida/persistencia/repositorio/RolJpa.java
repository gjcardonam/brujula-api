package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.RolEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolJpa extends JpaRepository<RolEntidad, Long> {
    Optional<RolEntidad> findByNombre(String nombre);
}
