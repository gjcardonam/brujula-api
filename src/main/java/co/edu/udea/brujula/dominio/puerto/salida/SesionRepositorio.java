package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.SesionRevocada;

import java.time.Instant;

public interface SesionRepositorio {

    void revocar(SesionRevocada sesion);

    boolean estaRevocada(String jti);

    int borrarExpiradas(Instant ahora);
}
