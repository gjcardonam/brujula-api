package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;

import java.time.Instant;

public interface AbrirEjercicio {

    record ParaPracticar(Ejercicio ejercicio, long intentosPrevios, int intentosRestantes, Instant disponibleDesde) {
    }

    ParaPracticar abrir(Long idEstudiante, Long idEjercicio);
}
