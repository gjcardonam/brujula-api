package co.edu.udea.brujula.practica;

import co.edu.udea.brujula.catalogo.ParametrosService;
import co.edu.udea.brujula.catalogo.TipoError;
import co.edu.udea.brujula.catalogo.TipoErrorRepository;
import co.edu.udea.brujula.ejercicio.Ejercicio;
import co.edu.udea.brujula.ejercicio.OpcionRespuesta;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Motor de reglas (sin ML) que clasifica un intento incorrecto (HU-027, HU-028).
 *
 * Orden de evaluación:
 *  1. Confianza 4 o 5 y respuesta incorrecta → "Error de ansiedad" (HU-027 CA-03).
 *  2. Dominio previo: en los últimos N intentos sobre la misma competencia o componente
 *     (práctica libre y simulacros, HU-028 CA-05) el porcentaje de aciertos es ≥ umbral y hay al
 *     menos el mínimo de intentos → "Error de hábito" (HU-027 CA-04, HU-028 CA-01 a CA-04).
 *  3. En otro caso, se toma la clasificación del distractor elegido (objetivo específico 1:
 *     los distractores se registran con el tipo de error que representan) y, si no tiene,
 *     "Error cognitivo" por defecto (HU-027 CA-05).
 *
 * Se ejecuta una sola vez al registrar el intento y el resultado se guarda con él (CA-06, CA-09).
 */
@Service
public class ClasificadorErrorService {

    private final IntentoRepository intentos;
    private final TipoErrorRepository tiposError;
    private final ParametrosService parametros;

    public ClasificadorErrorService(IntentoRepository intentos, TipoErrorRepository tiposError, ParametrosService parametros) {
        this.intentos = intentos;
        this.tiposError = tiposError;
        this.parametros = parametros;
    }

    public TipoError clasificar(Long idUsuario, Ejercicio ejercicio, OpcionRespuesta opcion, int nivelConfianza) {
        if (nivelConfianza >= 4) {
            return tipo(TipoError.ANSIEDAD);
        }
        if (dominaElArea(idUsuario, ejercicio)) {
            return tipo(TipoError.HABITO);
        }
        if (opcion.getTipoError() != null) {
            return opcion.getTipoError();
        }
        return tipo(TipoError.COGNITIVO);
    }

    /** HU-028: ventana de los últimos N intentos sobre la misma competencia o componente. */
    boolean dominaElArea(Long idUsuario, Ejercicio ejercicio) {
        int ventana = parametros.entero(ParametrosService.VENTANA_INTENTOS, 5);
        int minimo = parametros.entero(ParametrosService.MINIMO_INTENTOS_VENTANA, 3);
        int umbral = parametros.entero(ParametrosService.UMBRAL_DOMINIO_PCT, 70);
        List<Boolean> previos = intentos.ventanaDominio(idUsuario, ejercicio.getComponente().getId(),
                ejercicio.getCompetencia().getId(), PageRequest.of(0, Math.max(1, ventana)));
        if (previos.size() < minimo) return false;                                   // CA-04: datos insuficientes
        long aciertos = previos.stream().filter(Boolean::booleanValue).count();
        double pct = aciertos * 100.0 / previos.size();
        return pct >= umbral;                                                          // CA-02
    }

    private TipoError tipo(String nombre) {
        return tiposError.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("Falta el tipo de error '" + nombre + "' en la base de datos"));
    }
}
