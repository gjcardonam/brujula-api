package co.edu.udea.brujula.dominio.servicio;

import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.TipoError;

import java.util.List;

/**
 * Motor de reglas que decide por qué falló un intento (HU-027 y HU-028). No usa aprendizaje
 * automático: es una cadena de tres reglas que se evalúan en orden.
 *
 * Recibe la ventana de resultados previos ya consultada, así que se puede probar sola, sin base
 * de datos.
 */
public final class ClasificadorDeError {

    /** Valores que las historias piden dejar configurables (HU-028 CA-01, CA-02 y CA-04). */
    public record Ventana(int tamano, int minimoDeIntentos, int umbralDeDominioPct) {
    }

    private ClasificadorDeError() {
    }

    /**
     * @param resultadosPrevios resultados del estudiante en la misma competencia o componente, del
     *                          más reciente al más antiguo
     */
    public static String clasificar(int nivelConfianza, Opcion opcionElegida,
                                    List<Boolean> resultadosPrevios, Ventana ventana) {
        // Estaba seguro y falló: no es que no sepa, es que se confió o se puso nervioso.
        if (nivelConfianza >= 4) {
            return TipoError.ANSIEDAD;
        }
        if (dominaElArea(resultadosPrevios, ventana)) {
            return TipoError.HABITO;
        }
        // Si el distractor viene clasificado por quien creó el ejercicio, se respeta esa clasificación.
        if (opcionElegida.tipoError() != null) {
            return opcionElegida.tipoError().nombre();
        }
        return TipoError.COGNITIVO;
    }

    /**
     * Se considera que domina el área si venía acertando por encima del umbral. Con muy pocos
     * intentos previos no hay con qué comparar y no se evalúa (HU-028 CA-04).
     */
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
