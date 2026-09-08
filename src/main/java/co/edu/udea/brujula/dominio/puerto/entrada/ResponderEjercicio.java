package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;

import java.util.UUID;

/**
 * HU-010 a HU-012 en práctica libre y HU-014 dentro de un simulacro. De paso clasifica el error
 * (HU-027) y evita duplicados si se reenvía la misma respuesta (HU-016).
 */
public interface ResponderEjercicio {

    record Respuesta(Long idEjercicio, Long idOpcion, Integer nivelConfianza, UUID tokenIdempotencia,
                     Long idSimulacro) {
    }

    ResultadoDeIntento responder(Long idEstudiante, Respuesta respuesta);
}
