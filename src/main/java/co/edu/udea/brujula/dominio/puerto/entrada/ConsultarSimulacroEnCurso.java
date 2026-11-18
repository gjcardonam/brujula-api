package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;

import java.util.Optional;

public interface ConsultarSimulacroEnCurso {

    Optional<EstadoDelSimulacro> consultar(Long idEstudiante);
}
