package co.edu.udea.brujula.dominio.modelo.consulta;

import java.time.Instant;

public record ResultadoDeIntento(Long idIntento, boolean esCorrecto, Long idOpcionSeleccionada,
                                 Long idOpcionCorrecta, String retroalimentacion,
                                 boolean retroalimentacionDisponible, int nivelConfianza, Instant respondidoEn,
                                 long numeroIntento, boolean repetido, String tipoError) {
    public ResultadoDeIntento(Long idIntento, boolean esCorrecto, Long idOpcionSeleccionada,
                                 Long idOpcionCorrecta, String retroalimentacion,
                                 boolean retroalimentacionDisponible, int nivelConfianza, Instant respondidoEn,
                                 long numeroIntento, boolean repetido) {
        this(idIntento, esCorrecto, idOpcionSeleccionada, idOpcionCorrecta, retroalimentacion, retroalimentacionDisponible, nivelConfianza, respondidoEn, numeroIntento, repetido, null);
    }

}
