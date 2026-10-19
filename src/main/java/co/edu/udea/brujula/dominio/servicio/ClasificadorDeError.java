package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.TipoError;

import java.util.List;

public final class ClasificadorDeError {

    public record Ventana(int tamano, int minimoDeIntentos, int umbralDeDominioPct) {
    }

    private ClasificadorDeError() {
    }

    public static String clasificar(int nivelConfianza, Opcion opcionElegida,
                                    List<Boolean> resultadosPrevios, Ventana ventana) {
        if (nivelConfianza >= 4) {
            return TipoError.ANSIEDAD;
        }
        if (dominaElArea(resultadosPrevios, ventana)) {
            return TipoError.HABITO;
        }
        if (opcionElegida.tipoError() != null) {
            return opcionElegida.tipoError().nombre();
        }
        return TipoError.COGNITIVO;
    }

    public static boolean dominaElArea(List<Boolean> resultadosPrevios, Ventana ventana) {
        List<Boolean> considerados = resultadosPrevios.size() > ventana.tamano()
                ? resultadosPrevios.subList(0, ventana.tamano())
                : resultadosPrevios;
        if (considerados.size() < ventana.minimoDeIntentos()) {
            return false;
        }
        long aciertos = considerados.stream().filter(Boolean::booleanValue).count();
        return aciertos * 100.0 / considerados.size() >= ventana.umbralDeDominioPct();
    }
}
