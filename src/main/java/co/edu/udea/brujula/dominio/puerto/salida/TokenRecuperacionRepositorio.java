package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;

import java.time.Instant;
import java.util.Optional;

public interface TokenRecuperacionRepositorio {

    TokenRecuperacion guardar(TokenRecuperacion token);

    Optional<TokenRecuperacion> porHash(String hash);

    /** Para limitar cuántos enlaces se piden por hora (HU-003 CA-09). */
    long solicitudesDesde(Long idUsuario, Instant desde);
}
