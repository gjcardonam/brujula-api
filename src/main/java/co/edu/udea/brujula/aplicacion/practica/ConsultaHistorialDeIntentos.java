package co.edu.udea.brujula.aplicacion.practica;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarHistorialDeIntentos;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaHistorialDeIntentos implements ConsultarHistorialDeIntentos {

    private static final int TAMANO_PAGINA = 20;

    private final IntentoRepositorio intentos;

    public ConsultaHistorialDeIntentos(IntentoRepositorio intentos) {
        this.intentos = intentos;
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<ResumenDeIntento> listar(Long idEstudiante, int pagina) {
        return intentos.historialDe(idEstudiante, Math.max(0, pagina), TAMANO_PAGINA);
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleDeIntento detalle(Long idEstudiante, Long idIntento) {
        return intentos.detalle(idIntento, idEstudiante)
                .orElseThrow(() -> new NoEncontrado("El intento no existe."));
    }
}
