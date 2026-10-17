package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;

public interface ConsultarEstadisticas {

    Estadisticas consultar(Long idEstudiante);
}
