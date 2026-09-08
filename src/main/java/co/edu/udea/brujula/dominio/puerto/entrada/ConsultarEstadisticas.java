package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.consulta.Estadisticas;

/** HU-019 y HU-029. El filtro por componente aplica solo a la distribución de errores. */
public interface ConsultarEstadisticas {
    Estadisticas deEstudiante(Long idEstudiante, Long idComponenteFiltro);
}
