package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;

import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.SesionRevocadaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface SesionRevocadaJpa extends JpaRepository<SesionRevocadaEntidad, String> {

    @Modifying
    @Query("delete from SesionRevocadaEntidad s where s.expiraEn < :ahora")
    int borrarVencidas(Instant ahora);
}
