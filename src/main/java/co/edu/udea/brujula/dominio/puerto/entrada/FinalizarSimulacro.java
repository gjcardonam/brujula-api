package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;

public interface FinalizarSimulacro {

    ResultadoDeSimulacro finalizar(Long idEstudiante, Long idSimulacro);
}
