package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;
import co.edu.udea.brujula.dominio.puerto.salida.ErroresDelEstudianteRepositorio;
import co.edu.udea.brujula.dominio.modelo.consulta.ErrorRegistrado;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.IntentoJpa;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class ErroresDelEstudianteAdaptador implements ErroresDelEstudianteRepositorio {
    private final IntentoJpa intentos;
    public ErroresDelEstudianteAdaptador(IntentoJpa intentos) { this.intentos = intentos; }
    public List<ErrorRegistrado> delEstudiante(Long idEstudiante) {
        return intentos.erroresDelEstudiante(idEstudiante).stream()
            .map(r -> new ErrorRegistrado((Long) r[0], (String) r[1], (String) r[2])).toList();
    }
}
