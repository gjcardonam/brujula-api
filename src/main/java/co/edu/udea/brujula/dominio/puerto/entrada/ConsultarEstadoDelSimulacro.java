package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;

public interface ConsultarEstadoDelSimulacro {

    EstadoDelSimulacro consultar(Long idEstudiante, Long idSimulacro);
}
