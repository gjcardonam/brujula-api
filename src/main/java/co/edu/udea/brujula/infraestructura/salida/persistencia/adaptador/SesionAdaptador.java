package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.SesionRevocada;
import co.edu.udea.brujula.dominio.puerto.salida.SesionRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.SesionRevocadaEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.SesionRevocadaJpa;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SesionAdaptador implements SesionRepositorio {

    private final SesionRevocadaJpa sesiones;

    public SesionAdaptador(SesionRevocadaJpa sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    public void revocar(SesionRevocada sesion) {
        SesionRevocadaEntidad entidad = new SesionRevocadaEntidad();
        entidad.setJti(sesion.jti());
        entidad.setIdUsuario(sesion.idUsuario());
        entidad.setRevocadaEn(sesion.revocadaEn());
        entidad.setExpiraEn(sesion.expiraEn());
        sesiones.save(entidad);
    }

    @Override
    public boolean estaRevocada(String jti) {
        return sesiones.existsById(jti);
    }

    @Override
    public int borrarExpiradas(Instant ahora) {
        return sesiones.borrarVencidas(ahora);
    }
}
