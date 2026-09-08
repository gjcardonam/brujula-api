package co.edu.udea.brujula.dominio.modelo.consulta;

import java.time.Instant;

/** Fila del historial de ejercicios resueltos (HU-025 CA-02). */
public record ResumenDeIntento(Long id, Long idEjercicio, Integer numeroEjercicio, String componente,
                               String competencia, String nivel, boolean esCorrecto, Instant fechaHora,
                               String estadoEjercicio, boolean enSimulacro) {
}
