package co.edu.udea.brujula.dominio.puerto.entrada;

/** HU-010 CA-07 a CA-09: seguir practicando sin volver al banco. */
public interface BuscarSiguienteEjercicio {

    record Siguiente(boolean hayMas, Long idEjercicio, String mensaje) {
    }

    Siguiente buscar(Long idEstudiante, Long idEjercicioActual, Long idComponente);
}
