package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;

public interface ListarEjercicios {

    Pagina<TarjetaDeEjercicio> listar(boolean esAdministrador, Long idComponente, int pagina);
}
