package co.edu.udea.brujula.dominio.puerto.entrada.comando;

import java.util.List;

public record DatosDeEjercicio(String enunciado, String imagen, Long idComponente, Long idCompetencia, Long idNivel,
                               List<DatosDeOpcion> opciones) {

    public record DatosDeOpcion(Long id, String texto, String imagen, boolean correcta, String retroalimentacion,
                                Long idTipoError) {

        public DatosDeOpcion(String texto, String imagen, boolean correcta, String retroalimentacion) {
            this(null, texto, imagen, correcta, retroalimentacion, null);
        }
    }
}
