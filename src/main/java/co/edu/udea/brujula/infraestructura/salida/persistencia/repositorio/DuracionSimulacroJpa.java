package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.DuracionSimulacroEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DuracionSimulacroJpa extends JpaRepository<DuracionSimulacroEntidad, Long> {
    List<DuracionSimulacroEntidad> findAllByOrderByMinutosAsc();
}
