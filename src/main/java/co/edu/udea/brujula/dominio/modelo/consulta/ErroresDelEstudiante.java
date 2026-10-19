package co.edu.udea.brujula.dominio.modelo.consulta;
import java.util.List;
public record ErroresDelEstudiante(long totalIncorrectos, long sinClasificar,
        List<DistribucionDeError> distribucion, String tipoPredominante, String mensajeOrientador,
        Long idComponenteFiltro, List<Componente> componentesConIntentos) {
    public record Componente(Long id, String nombre) {}
}
