package co.edu.udea.brujula.aplicacion.practica;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarErroresDelEstudiante;
import co.edu.udea.brujula.dominio.puerto.salida.ErroresDelEstudianteRepositorio;
import co.edu.udea.brujula.dominio.modelo.consulta.ErroresDelEstudiante;
import co.edu.udea.brujula.dominio.servicio.DistribucionDeErrores;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ConsultaDeErroresDelEstudiante implements ConsultarErroresDelEstudiante {
    private final ErroresDelEstudianteRepositorio errores;
    public ConsultaDeErroresDelEstudiante(ErroresDelEstudianteRepositorio errores) { this.errores = errores; }
    @Transactional(readOnly = true)
    public ErroresDelEstudiante consultar(Long idEstudiante, Long idComponente) {
        return DistribucionDeErrores.calcular(errores.delEstudiante(idEstudiante), idComponente);
    }
}
