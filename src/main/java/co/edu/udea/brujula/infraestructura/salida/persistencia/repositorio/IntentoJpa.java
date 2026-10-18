package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IntentoJpa extends JpaRepository<IntentoEntidad, Long> {

    Optional<IntentoEntidad> findByTokenIdempotencia(UUID token);

    long countByIdUsuarioAndEjercicio_Id(Long idUsuario, Long idEjercicio);
}
