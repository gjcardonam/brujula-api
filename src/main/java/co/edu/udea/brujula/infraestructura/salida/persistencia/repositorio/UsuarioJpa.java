package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.UsuarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioJpa extends JpaRepository<UsuarioEntidad, Long> {
    Optional<UsuarioEntidad> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRol_Nombre(String nombreDelRol);
}
