package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.SimulacroEntidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SimulacroJpa extends JpaRepository<SimulacroEntidad, Long> {

    Optional<SimulacroEntidad> findByIdAndIdUsuario(Long id, Long idUsuario);

    List<SimulacroEntidad> findByIdUsuarioAndEstado(Long idUsuario, String estado);

    List<SimulacroEntidad> findByIdUsuarioAndEstadoOrderByInicioDesc(Long idUsuario, String estado);

    Page<SimulacroEntidad> findByIdUsuarioAndEstadoOrderByInicioDesc(Long idUsuario, String estado, Pageable pageable);
}
