package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;

public interface ConsultarResultadoDelSimulacro {

    ResultadoDeSimulacro consultar(Long idEstudiante, Long idSimulacro);
}
