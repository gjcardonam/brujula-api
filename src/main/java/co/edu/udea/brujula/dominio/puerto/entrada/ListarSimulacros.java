package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeSimulacro;

public interface ListarSimulacros {

    Pagina<ResumenDeSimulacro> listar(Long idEstudiante, int pagina);
}
