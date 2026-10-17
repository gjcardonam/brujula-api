package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;

public interface IniciarSimulacro {

    EstadoDelSimulacro iniciar(Long idEstudiante, Long idDuracion);
}
