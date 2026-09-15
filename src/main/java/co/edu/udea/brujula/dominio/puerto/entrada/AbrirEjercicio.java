package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;

public interface AbrirEjercicio {

    record ParaPracticar(Ejercicio ejercicio, long intentosPrevios) {
    }

    ParaPracticar abrir(Long idEstudiante, Long idEjercicio);
}
