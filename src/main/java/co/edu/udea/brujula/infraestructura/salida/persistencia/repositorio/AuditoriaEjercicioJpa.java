package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.AuditoriaEjercicioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaEjercicioJpa extends JpaRepository<AuditoriaEjercicioEntidad, Long> {
}
