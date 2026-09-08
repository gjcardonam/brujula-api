package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.Recomendacion;
import co.edu.udea.brujula.dominio.modelo.Simulacro;

import java.util.List;
import java.util.Optional;

public interface SimulacroRepositorio {

    Simulacro guardar(Simulacro simulacro);

    Optional<Simulacro> deEstudiante(Long idSimulacro, Long idEstudiante);

    List<Simulacro> enCursoDe(Long idEstudiante);

    List<Simulacro> finalizadosDe(Long idEstudiante);

    Pagina<Simulacro> finalizadosDe(Long idEstudiante, int pagina, int tamano);

    void guardarRecomendaciones(Long idSimulacro, List<Recomendacion> recomendaciones);

    List<Recomendacion> recomendacionesDe(Long idSimulacro);
}
