package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.TokenRecuperacion;

import java.time.Instant;
import java.util.Optional;

public interface TokenRecuperacionRepositorio {

    TokenRecuperacion guardar(TokenRecuperacion token);

    Optional<TokenRecuperacion> porHash(String hash);

    long solicitudesDesde(Long idUsuario, Instant desde);
}
