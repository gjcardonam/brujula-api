package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.puerto.entrada.DepurarSesionesRevocadas;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SesionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepuracionDeSesionesRevocadas implements DepurarSesionesRevocadas {

    private final SesionRepositorio sesiones;
    private final Reloj reloj;

    public DepuracionDeSesionesRevocadas(SesionRepositorio sesiones, Reloj reloj) {
        this.sesiones = sesiones;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public int depurar() {
        return sesiones.borrarExpiradas(reloj.ahora());
    }
}
