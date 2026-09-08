package co.edu.udea.brujula.practica;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IntentoRepository extends JpaRepository<Intento, Long> {

    Optional<Intento> findByTokenIdempotencia(UUID token);

    long countByUsuario_IdAndEjercicio_Id(Long idUsuario, Long idEjercicio);

    long countByEjercicio_Id(Long idEjercicio);

    long countByEjercicio_IdAndEsCorrectoTrue(Long idEjercicio);

    boolean existsByOpcionSeleccionada_Id(Long idOpcion);

    boolean existsBySimulacro_IdAndEjercicio_Id(Long idSimulacro, Long idEjercicio);

    /** Ids de opciones ya usadas en intentos de un ejercicio (HU-021 CA-09). */
    @Query("select distinct i.opcionSeleccionada.id from Intento i where i.ejercicio.id = :idEjercicio")
    List<Long> opcionesUsadas(@Param("idEjercicio") Long idEjercicio);

    /** Conteo de intentos por ejercicio para las tarjetas del administrador. */
    @Query("select i.ejercicio.id, count(i) from Intento i where i.ejercicio.id in :ids group by i.ejercicio.id")
    List<Object[]> contarPorEjercicio(@Param("ids") List<Long> ids);

    /** Distribución de tipos de error de un ejercicio (HU-022 CA-03). */
    @Query("select i.tipoError.nombre, count(i) from Intento i where i.ejercicio.id = :idEjercicio and i.esCorrecto = false and i.tipoError is not null group by i.tipoError.nombre")
    List<Object[]> erroresPorEjercicio(@Param("idEjercicio") Long idEjercicio);

    /** Ventana de HU-028: últimos intentos del estudiante sobre la misma competencia o componente, previos al actual. */
    @Query("select i.esCorrecto from Intento i where i.usuario.id = :idUsuario " +
           "and (i.ejercicio.componente.id = :idComponente or i.ejercicio.competencia.id = :idCompetencia) " +
           "order by i.fechaHora desc, i.id desc")
    List<Boolean> ventanaDominio(@Param("idUsuario") Long idUsuario, @Param("idComponente") Long idComponente,
                                 @Param("idCompetencia") Long idCompetencia, Pageable pageable);

    /** Historial (HU-025): del más reciente al más antiguo. */
    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "ejercicio.nivelDificultad"})
    Page<Intento> findByUsuario_IdOrderByFechaHoraDescIdDesc(Long idUsuario, Pageable pageable);

    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "ejercicio.nivelDificultad",
            "ejercicio.opciones", "opcionSeleccionada", "tipoError"})
    Optional<Intento> findByIdAndUsuario_Id(Long id, Long idUsuario);

    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "ejercicio.nivelDificultad",
            "ejercicio.opciones", "opcionSeleccionada"})
    List<Intento> findBySimulacro_IdOrderByFechaHoraAscIdAsc(Long idSimulacro);

    long countBySimulacro_Id(Long idSimulacro);

    long countBySimulacro_IdAndEsCorrectoTrue(Long idSimulacro);

    /** Todos los intentos del estudiante con lo necesario para estadísticas (HU-019, HU-029). */
    @EntityGraph(attributePaths = {"ejercicio", "ejercicio.componente", "ejercicio.competencia", "tipoError"})
    List<Intento> findByUsuario_Id(Long idUsuario);
}
