package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.modelo.SesionRevocada;
import co.edu.udea.brujula.dominio.puerto.salida.SesionRepositorio;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class SesionesEnMemoria implements SesionRepositorio {

    private final Map<String, SesionRevocada> revocadas = new LinkedHashMap<>();

    public Map<String, SesionRevocada> todas() {
        return revocadas;
    }

    @Override
    public void revocar(SesionRevocada sesion) {
        revocadas.put(sesion.jti(), sesion);
    }

    @Override
    public boolean estaRevocada(String jti) {
        return revocadas.containsKey(jti);
    }

    @Override
    public int borrarExpiradas(Instant ahora) {
        int antes = revocadas.size();
        revocadas.values().removeIf(sesion -> sesion.expiraEn().isBefore(ahora));
        return antes - revocadas.size();
    }
}
