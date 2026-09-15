package co.edu.udea.brujula.dominio.puerto.entrada.comando;

import java.util.List;

public record DatosDeEjercicio(String enunciado, String imagen, Long idComponente, Long idCompetencia, Long idNivel,
                               List<DatosDeOpcion> opciones) {

    public record DatosDeOpcion(String texto, String imagen, boolean correcta, String retroalimentacion) {
    }
}
