package co.edu.udea.brujula.aplicacion.administracion;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarDetalleDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaDetalleDeEjercicio implements ConsultarDetalleDeEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final IntentoRepositorio intentos;

    public ConsultaDetalleDeEjercicio(EjercicioRepositorio ejercicios, IntentoRepositorio intentos) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
    }

    @Override
    @Transactional(readOnly = true)
    public DetalleDeEjercicio detalle(Long idEjercicio) {
        return new DetalleDeEjercicio(
                ejercicios.porId(idEjercicio).orElseThrow(() -> new NoEncontrado("El ejercicio no existe.")),
                intentos.usoDe(idEjercicio), intentos.opcionesUsadas(idEjercicio));
    }
}
