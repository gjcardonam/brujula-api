package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ListarEjercicios;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListadoDeEjercicios implements ListarEjercicios {

    private final EjercicioRepositorio ejercicios;
    private final ParametrosDelSistema parametros;

    public ListadoDeEjercicios(EjercicioRepositorio ejercicios, ParametrosDelSistema parametros) {
        this.ejercicios = ejercicios;
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<TarjetaDeEjercicio> listar(boolean esAdministrador, Long idComponente, int pagina) {
        int tamano = parametros.entero(ParametrosDelSistema.TAMANO_PAGINA_BANCO, 20);
        Pagina<Ejercicio> encontrados = ejercicios.buscar(idComponente, !esAdministrador, Math.max(0, pagina), tamano);
        return encontrados.mapear(ejercicio -> new TarjetaDeEjercicio(ejercicio.id(), ejercicio.numero(),
                ejercicio.componente().nombre(), ejercicio.competencia().nombre(), ejercicio.nivel().nombre(),
                ejercicio.estado()));
    }
}
