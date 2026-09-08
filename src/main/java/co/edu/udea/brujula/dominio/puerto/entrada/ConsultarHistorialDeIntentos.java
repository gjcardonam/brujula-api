package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;

/** HU-025. */
public interface ConsultarHistorialDeIntentos {

    Pagina<ResumenDeIntento> listar(Long idEstudiante, int pagina);

    DetalleDeIntento detalle(Long idEstudiante, Long idIntento);
}
