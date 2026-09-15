package co.edu.udea.brujula.dominio.servicio;

import java.util.ArrayList;
import java.util.List;

public final class PoliticaDeNombres {

    private PoliticaDeNombres() {
    }

    public static List<String> revisar(String nombre, String apellido) {
        List<String> errores = new ArrayList<>();
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        String apellidoLimpio = apellido == null ? "" : apellido.trim();
        if (nombreLimpio.isEmpty() || nombreLimpio.length() > 30) {
            errores.add("El nombre debe tener entre 1 y 30 caracteres.");
        }
        if (apellidoLimpio.length() < 2 || apellidoLimpio.length() > 50) {
            errores.add("El apellido debe tener entre 2 y 50 caracteres.");
        }
        return errores;
    }
}
