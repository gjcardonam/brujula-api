package co.edu.udea.brujula.simulacro;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SimulacroRepository extends JpaRepository<Simulacro, Long> {
    Optional<Simulacro> findByIdAndUsuario_Id(Long id, Long idUsuario);
    List<Simulacro> findByUsuario_IdAndEstado(Long idUsuario, String estado);
    Page<Simulacro> findByUsuario_IdAndEstadoOrderByFechaInicioDesc(Long idUsuario, String estado, Pageable pageable);
    List<Simulacro> findByUsuario_IdAndEstadoOrderByFechaInicioDesc(Long idUsuario, String estado);
}
