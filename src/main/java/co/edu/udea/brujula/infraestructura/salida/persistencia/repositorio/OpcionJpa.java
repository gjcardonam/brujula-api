package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.OpcionEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpcionJpa extends JpaRepository<OpcionEntidad, Long> {
}
