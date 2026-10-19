package co.edu.udea.brujula.dominio.puerto.entrada;
import co.edu.udea.brujula.dominio.modelo.consulta.ErroresDelEstudiante;
public interface ConsultarErroresDelEstudiante {
    ErroresDelEstudiante consultar(Long idEstudiante, Long idComponente);
}
