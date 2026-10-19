package co.edu.udea.brujula.dominio.modelo.consulta;

import java.time.Instant;

public record ResumenDeIntento(Long id, Long idEjercicio, Integer numeroEjercicio, String componente,
                               String competencia, String nivel, boolean esCorrecto, Instant fechaHora,
                               String estadoEjercicio) {
}
