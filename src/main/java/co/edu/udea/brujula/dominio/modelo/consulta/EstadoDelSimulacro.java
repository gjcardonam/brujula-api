package co.edu.udea.brujula.dominio.modelo.consulta;

import co.edu.udea.brujula.dominio.modelo.Simulacro;

import java.time.Instant;

public record EstadoDelSimulacro(Long id, String estado, int duracionMinutos, Instant fechaInicio,
                                 Instant finPrevisto, Instant ahora, long tiempoRestanteSeg, long respondidos,
                                 long correctas) {

    public static EstadoDelSimulacro de(Simulacro simulacro, Instant ahora, long respondidos, long correctas) {
        return new EstadoDelSimulacro(simulacro.id(), simulacro.estado(), simulacro.duracion().minutos(),
                simulacro.inicio(), simulacro.finPrevisto(), ahora, simulacro.segundosRestantes(ahora), respondidos,
                correctas);
    }
}
