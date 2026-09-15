package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.modelo.SesionRevocada;
import co.edu.udea.brujula.dominio.puerto.entrada.CerrarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SesionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class CierreDeSesion implements CerrarSesion {

    private static final Duration VIGENCIA_SUPUESTA = Duration.ofHours(3);

    private final SesionRepositorio sesiones;
    private final Reloj reloj;

    public CierreDeSesion(SesionRepositorio sesiones, Reloj reloj) {
        this.sesiones = sesiones;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void cerrar(Long idUsuario, String jti, Instant expiraEn) {
        if (sesiones.estaRevocada(jti)) return;
        Instant ahora = reloj.ahora();
        Instant vence = expiraEn != null ? expiraEn : ahora.plus(VIGENCIA_SUPUESTA);
        sesiones.revocar(new SesionRevocada(jti, idUsuario, ahora, vence));
    }
}
