package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.SimulacroEntidad;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SimulacroJpa extends JpaRepository<SimulacroEntidad, Long> {

    Optional<SimulacroEntidad> findByIdAndIdUsuario(Long id, Long idUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SimulacroEntidad s where s.id = :id and s.idUsuario = :idUsuario")
    Optional<SimulacroEntidad> bloquearDelUsuario(@Param("id") Long id, @Param("idUsuario") Long idUsuario);

    List<SimulacroEntidad> findByIdUsuarioAndEstado(Long idUsuario, String estado);

    List<SimulacroEntidad> findByIdUsuarioAndEstadoOrderByInicioDesc(Long idUsuario, String estado);

    Page<SimulacroEntidad> findByIdUsuarioAndEstadoOrderByInicioDescIdDesc(Long idUsuario, String estado,
                                                                          Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update SimulacroEntidad s set s.estado = 'Finalizado', s.fin = :fin, "
            + "s.tiempoUtilizadoSeg = :tiempoUtilizadoSeg where s.id = :id and s.estado = 'En curso'")
    int finalizar(@Param("id") Long id, @Param("fin") Instant fin,
                  @Param("tiempoUtilizadoSeg") Integer tiempoUtilizadoSeg);
}
