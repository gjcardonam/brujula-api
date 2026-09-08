package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;

/** HU-006, HU-007 y HU-008: el banco con su filtro por componente y su paginación. */
public interface ConsultarBanco {

    Pagina<TarjetaDeEjercicio> listar(boolean esAdministrador, Long idComponente, int pagina);

    ComponentesDelBanco componentes(boolean esAdministrador);
}
