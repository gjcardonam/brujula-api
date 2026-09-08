package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.ParametroSistemaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametroSistemaJpa extends JpaRepository<ParametroSistemaEntidad, String> {
}
