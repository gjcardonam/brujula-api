package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeIntento;

import java.util.UUID;

public interface RegistrarIntento {

    record Respuesta(Long idEjercicio, Long idOpcion, Integer nivelConfianza, UUID tokenIdempotencia) {
    }

    ResultadoDeIntento registrar(Long idEstudiante, Respuesta respuesta);
}
