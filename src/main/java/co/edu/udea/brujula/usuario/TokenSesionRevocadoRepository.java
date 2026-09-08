package co.edu.udea.brujula.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface TokenSesionRevocadoRepository extends JpaRepository<TokenSesionRevocado, String> {
    @Modifying
    @Query("delete from TokenSesionRevocado t where t.expiraEn < :ahora")
    int eliminarExpirados(Instant ahora);
}
