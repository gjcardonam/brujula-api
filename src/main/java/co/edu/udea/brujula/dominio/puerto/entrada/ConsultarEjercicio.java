package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;

/** HU-009: abrir un ejercicio para resolverlo. */
public interface ConsultarEjercicio {

    record ParaPracticar(Ejercicio ejercicio, long intentosPrevios) {
    }

    ParaPracticar paraPracticar(Long idEstudiante, Long idEjercicio);
}
