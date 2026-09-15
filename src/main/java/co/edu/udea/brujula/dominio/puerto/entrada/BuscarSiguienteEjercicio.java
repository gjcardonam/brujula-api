package co.edu.udea.brujula.dominio.puerto.entrada;

public interface BuscarSiguienteEjercicio {

    record Siguiente(boolean hayMas, Long idEjercicio, String mensaje) {
    }

    Siguiente buscar(Long idEstudiante, Long idEjercicioActual, Long idComponente);
}
