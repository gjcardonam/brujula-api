package co.edu.udea.brujula.dominio.modelo.consulta;

import java.time.Instant;

/**
 * Estado del temporizador y del progreso. Se manda la hora del servidor para que el navegador
 * pueda descontar el desfase de su propio reloj.
 */
public record EstadoDelSimulacro(Long id, String estado, int duracionMinutos, Instant fechaInicio,
                                 Instant finPrevisto, Instant ahora, long tiempoRestanteSeg,
                                 long respondidos, long correctas) {
}
