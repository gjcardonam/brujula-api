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

    @Query("select i.componente.id, i.componente.nombre, t.nombre "
            + "from IntentoEntidad i left join i.tipoError t where i.idUsuario = :idUsuario and i.correcto = false")
    List<Object[]> erroresDelEstudiante(@Param("idUsuario") Long idUsuario);

    Optional<IntentoEntidad> findByTokenIdempotencia(UUID token);

    long countByIdUsuarioAndEjercicio_Id(Long idUsuario, Long idEjercicio);

    long countByEjercicio_Id(Long idEjercicio);

    @Query("select count(i), max(i.respondidoEn) from IntentoEntidad i where i.idUsuario = :idUsuario "
            + "and i.ejercicio.id = :idEjercicio and i.idSimulacro is null")
    List<Object[]> practicaDe(@Param("idUsuario") Long idUsuario, @Param("idEjercicio") Long idEjercicio);

    long countByEjercicio_IdAndCorrectoTrue(Long idEjercicio);

    @Query("select distinct i.opcionSeleccionada.id from IntentoEntidad i where i.ejercicio.id = :idEjercicio")
    List<Long> opcionesUsadas(@Param("idEjercicio") Long idEjercicio);

    @Query("select i.tipoError.nombre, count(i) from IntentoEntidad i where i.ejercicio.id = :idEjercicio "
            + "and i.correcto = false and i.tipoError is not null group by i.tipoError.nombre")
    List<Object[]> erroresPorEjercicio(@Param("idEjercicio") Long idEjercicio);

    @Query("select i.correcto from IntentoEntidad i where i.idUsuario = :idUsuario "
            + "and (i.componente.id = :idComponente or i.competencia.id = :idCompetencia) "
            + "order by i.respondidoEn desc, i.id desc")
    List<Boolean> ultimosResultados(@Param("idUsuario") Long idUsuario, @Param("idComponente") Long idComponente,
                                    @Param("idCompetencia") Long idCompetencia, Pageable pageable);

    @EntityGraph(attributePaths = {"ejercicio", "componente", "competencia", "nivel"})
    Page<IntentoEntidad> findByIdUsuarioOrderByRespondidoEnDescIdDesc(Long idUsuario, Pageable pageable);

    @EntityGraph(attributePaths = {"ejercicio", "componente", "competencia", "nivel",
            "ejercicio.opciones", "opcionSeleccionada", "tipoError"})
    Optional<IntentoEntidad> findByIdAndIdUsuario(Long id, Long idUsuario);

    boolean existsByIdSimulacroAndEjercicio_Id(Long idSimulacro, Long idEjercicio);

    long countByIdSimulacro(Long idSimulacro);

    long countByIdSimulacroAndCorrectoTrue(Long idSimulacro);

    @EntityGraph(attributePaths = {"ejercicio", "componente", "competencia", "nivel",
            "ejercicio.opciones", "opcionSeleccionada", "tipoError"})
    List<IntentoEntidad> findByIdSimulacroOrderByRespondidoEnAscIdAsc(Long idSimulacro);

    @EntityGraph(attributePaths = {"ejercicio", "componente", "competencia", "nivel",
            "opcionSeleccionada", "tipoError"})
    List<IntentoEntidad> findByIdUsuario(Long idUsuario);
}
