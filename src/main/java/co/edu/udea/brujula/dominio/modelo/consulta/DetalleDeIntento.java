package co.edu.udea.brujula.dominio.modelo.consulta;

import java.time.Instant;
import java.util.List;

public record DetalleDeIntento(Long id, Long idEjercicio, Integer numeroEjercicio, String enunciado,
                               String imagenEnunciado, String componente, String competencia, String nivel,
                               String estadoEjercicio, List<OpcionRevisada> opciones, boolean esCorrecto,
                               int nivelConfianza, Instant fechaHora, String retroalimentacion) {

    public record OpcionRevisada(Long id, String letra, String descripcion, String imagen,
                                 boolean seleccionada, boolean esCorrecta) {
    }
}
