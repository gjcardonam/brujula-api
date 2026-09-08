package co.edu.udea.brujula.ejercicio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EjercicioRepository extends JpaRepository<Ejercicio, Long> {

    @EntityGraph(attributePaths = {"componente", "competencia", "nivelDificultad"})
    @Query("select e from Ejercicio e where (:soloActivos = false or e.estado = 'Activo') " +
           "and (:idComponente is null or e.componente.id = :idComponente) order by e.numero asc")
    Page<Ejercicio> listar(@Param("soloActivos") boolean soloActivos, @Param("idComponente") Long idComponente, Pageable pageable);

    @EntityGraph(attributePaths = {"componente", "competencia", "nivelDificultad", "opciones", "creador"})
    @Query("select e from Ejercicio e where e.id = :id")
    Optional<Ejercicio> buscarCompleto(@Param("id") Long id);

    /** Conteo por componente según lo que puede ver el rol (HU-007 CA-07). */
    @Query("select e.componente.id, e.componente.nombre, count(e) from Ejercicio e " +
           "where (:soloActivos = false or e.estado = 'Activo') group by e.componente.id, e.componente.nombre order by e.componente.id")
    List<Object[]> contarPorComponente(@Param("soloActivos") boolean soloActivos);

    @Query("select count(e) from Ejercicio e where (:soloActivos = false or e.estado = 'Activo')")
    long contarTotal(@Param("soloActivos") boolean soloActivos);

    @Query("select e.id from Ejercicio e where lower(trim(e.enunciado)) = lower(trim(:enunciado))")
    List<Long> idsPorEnunciado(@Param("enunciado") String enunciado);

    /**
     * Siguiente ejercicio en práctica libre (HU-010 CA-07/CA-08): otro ejercicio activo, del mismo
     * componente si hay filtro, prefiriendo los que el estudiante aún no ha intentado, en orden aleatorio.
     */
    @Query(value = """
            select e.id_ejercicio from ejercicios e
            where e.estado = 'Activo' and e.id_ejercicio <> :idActual
              and (cast(:idComponente as bigint) is null or e.id_componente = cast(:idComponente as bigint))
            order by (exists (select 1 from intentos i where i.id_ejercicio = e.id_ejercicio and i.id_usuario = :idUsuario)) asc,
                     random()
            limit 1
            """, nativeQuery = true)
    Optional<Long> siguienteParaPractica(@Param("idActual") Long idActual, @Param("idComponente") Long idComponente, @Param("idUsuario") Long idUsuario);

    /**
     * Siguiente ejercicio del simulacro (HU-014 CA-01/CA-07): activo, no respondido en este simulacro,
     * en un orden pseudoaleatorio fijo por simulacro para que una recarga muestre el mismo ejercicio.
     */
    @Query(value = """
            select e.id_ejercicio from ejercicios e
            where e.estado = 'Activo'
              and not exists (select 1 from intentos i where i.id_simulacro = :idSimulacro and i.id_ejercicio = e.id_ejercicio)
            order by md5(cast(:idSimulacro as text) || '-' || cast(e.id_ejercicio as text))
            limit 1
            """, nativeQuery = true)
    Optional<Long> siguienteParaSimulacro(@Param("idSimulacro") Long idSimulacro);

    long countByEstado(String estado);
}
