package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.SiguienteDelSimulacro;

public interface PresentarSiguienteEjercicio {

    SiguienteDelSimulacro presentar(Long idEstudiante, Long idSimulacro);
}
