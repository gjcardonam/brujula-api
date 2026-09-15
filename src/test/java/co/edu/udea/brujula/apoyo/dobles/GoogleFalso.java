package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.excepcion.ServicioNoDisponible;
import co.edu.udea.brujula.dominio.puerto.salida.VerificadorDeGoogle;

public class GoogleFalso implements VerificadorDeGoogle {

    private boolean disponible = true;

    public void caerse() {
        disponible = false;
    }

    @Override
    public CuentaDeGoogle verificar(String credencial) {
        if (!disponible) {
            throw new ServicioNoDisponible("GOOGLE_NO_DISPONIBLE", "El servicio de Google no respondió.");
        }
        return new CuentaDeGoogle("google-" + credencial, credencial, "Ana María", "Pérez Gómez");
    }

    @Override
    public boolean estaSimulado() {
        return true;
    }
}
