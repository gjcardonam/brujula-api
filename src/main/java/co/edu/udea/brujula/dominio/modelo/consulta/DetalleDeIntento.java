package co.edu.udea.brujula.dominio.modelo.consulta;

import java.time.Instant;
import java.util.List;

/** Detalle de un intento tal como quedó registrado; no se recalcula si el ejercicio cambia después. */
public record DetalleDeIntento(Long id, Long idEjercicio, Integer numeroEjercicio, String enunciado,
                               String imagenEnunciado, String componente, String competencia, String nivel,
                               String estadoEjercicio, List<OpcionRevisada> opciones, boolean esCorrecto,
                               int nivelConfianza, Instant fechaHora, String retroalimentacion, Long idSimulacro) {

    public record OpcionRevisada(Long id, String letra, String descripcion, String imagen,
                                 boolean seleccionada, boolean esCorrecta) {
    }
}
