package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntentoJpa extends JpaRepository<IntentoEntidad, Long> {

    Optional<IntentoEntidad> findByTokenIdempotencia(UUID token);

    long countByIdUsuarioAndEjercicio_Id(Long idUsuario, Long idEjercicio);

    boolean existsByIdSimulacroAndEjercicio_Id(Long idSimulacro, Long idEjercicio);

    long countByIdSimulacro(Long idSimulacro);

    long countByIdSimulacroAndCorrectoTrue(Long idSimulacro);

    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "ejercicio.nivel",
            "ejercicio.opciones", "opcionSeleccionada"})
    List<IntentoEntidad> findByIdSimulacroOrderByRespondidoEnAscIdAsc(Long idSimulacro);

    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "ejercicio.nivel",
            "opcionSeleccionada"})
    List<IntentoEntidad> findByIdUsuario(Long idUsuario);
}
