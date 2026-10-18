package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.EjercicioEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EjercicioJpa extends JpaRepository<EjercicioEntidad, Long> {

    @EntityGraph(attributePaths = {"componente", "competencia", "nivel"})
    @Query("select e from EjercicioEntidad e where (:soloActivos = false or e.estado = 'Activo') "
            + "and (:idComponente is null or e.componente.id = :idComponente) order by e.numero asc")
    Page<EjercicioEntidad> listar(@Param("soloActivos") boolean soloActivos,
                                  @Param("idComponente") Long idComponente, Pageable pageable);

    @EntityGraph(attributePaths = {"componente", "competencia", "nivel", "opciones", "creador"})
    @Query("select e from EjercicioEntidad e where e.id = :id")
    Optional<EjercicioEntidad> buscarCompleto(@Param("id") Long id);

    @Query("select e.componente.id, e.componente.nombre, count(e) from EjercicioEntidad e "
            + "where (:soloActivos = false or e.estado = 'Activo') "
            + "group by e.componente.id, e.componente.nombre order by e.componente.id")
    List<Object[]> contarPorComponente(@Param("soloActivos") boolean soloActivos);

    @Query("select count(e) from EjercicioEntidad e where (:soloActivos = false or e.estado = 'Activo')")
    long contarTotal(@Param("soloActivos") boolean soloActivos);

    @Query("select count(e) from EjercicioEntidad e where lower(trim(e.enunciado)) = lower(trim(:enunciado))")
    long contarConEnunciado(@Param("enunciado") String enunciado);

    @Query(value = """
            select e.id_ejercicio from ejercicios e
            where e.estado = 'Activo' and e.id_ejercicio <> :idActual
              and (cast(:idComponente as bigint) is null or e.id_componente = cast(:idComponente as bigint))
            order by (exists (select 1 from intentos i
                              where i.id_ejercicio = e.id_ejercicio and i.id_usuario = :idUsuario)) asc,
                     random()
            limit 1
            """, nativeQuery = true)
    Optional<Long> siguienteParaPractica(@Param("idActual") Long idActual,
                                         @Param("idComponente") Long idComponente,
                                         @Param("idUsuario") Long idUsuario);
}
