package co.edu.udea.brujula.dominio.puerto.salida;
import co.edu.udea.brujula.dominio.modelo.consulta.ErrorRegistrado;
import java.util.List;
public interface ErroresDelEstudianteRepositorio {
    List<ErrorRegistrado> delEstudiante(Long idEstudiante);
}
