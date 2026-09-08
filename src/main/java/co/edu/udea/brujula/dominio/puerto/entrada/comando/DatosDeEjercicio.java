package co.edu.udea.brujula.dominio.puerto.entrada.comando;

import java.util.List;

/** Lo que llega del formulario de crear o editar un ejercicio (HU-020, HU-021). */
public record DatosDeEjercicio(String enunciado, String imagen, Long idComponente, Long idCompetencia,
                               Long idNivel, List<DatosDeOpcion> opciones) {

    /** El id viene nulo cuando la opción es nueva. */
    public record DatosDeOpcion(Long id, String texto, String imagen, boolean correcta,
                                String retroalimentacion, Long idTipoError) {
    }
}
