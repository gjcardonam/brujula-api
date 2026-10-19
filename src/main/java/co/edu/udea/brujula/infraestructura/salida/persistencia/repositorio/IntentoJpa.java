package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.IntentoEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntentoJpa extends JpaRepository<IntentoEntidad, Long> {

    Optional<IntentoEntidad> findByTokenIdempotencia(UUID token);

    long countByIdUsuarioAndEjercicio_Id(Long idUsuario, Long idEjercicio);

    long countByEjercicio_Id(Long idEjercicio);

    long countByEjercicio_IdAndCorrectoTrue(Long idEjercicio);

    @Query("select distinct i.opcionSeleccionada.id from IntentoEntidad i where i.ejercicio.id = :idEjercicio")
    List<Long> opcionesUsadas(@Param("idEjercicio") Long idEjercicio);

    @Query("select i.tipoError.nombre, count(i) from IntentoEntidad i where i.ejercicio.id = :idEjercicio "
            + "and i.correcto = false and i.tipoError is not null group by i.tipoError.nombre")
    List<Object[]> erroresPorEjercicio(@Param("idEjercicio") Long idEjercicio);

    @Query("select i.correcto from IntentoEntidad i where i.idUsuario = :idUsuario "
            + "and (i.ejercicio.componente.id = :idComponente or i.ejercicio.competencia.id = :idCompetencia) "
            + "order by i.respondidoEn desc, i.id desc")
    List<Boolean> ultimosResultados(@Param("idUsuario") Long idUsuario, @Param("idComponente") Long idComponente,
                                    @Param("idCompetencia") Long idCompetencia, Pageable pageable);

    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "ejercicio.nivel"})
    Page<IntentoEntidad> findByIdUsuarioOrderByRespondidoEnDescIdDesc(Long idUsuario, Pageable pageable);

    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "ejercicio.nivel",
            "ejercicio.opciones", "opcionSeleccionada", "tipoError"})
    Optional<IntentoEntidad> findByIdAndIdUsuario(Long id, Long idUsuario);
}
