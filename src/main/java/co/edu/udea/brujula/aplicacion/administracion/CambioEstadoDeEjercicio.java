package co.edu.udea.brujula.aplicacion.administracion;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Auditoria;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.CambiarEstadoDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AuditoriaRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CambioEstadoDeEjercicio implements CambiarEstadoDeEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final IntentoRepositorio intentos;
    private final AuditoriaRepositorio auditoria;
    private final Reloj reloj;

    public CambioEstadoDeEjercicio(EjercicioRepositorio ejercicios, IntentoRepositorio intentos,
                                   AuditoriaRepositorio auditoria, Reloj reloj) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public DetalleDeEjercicio cambiarEstado(Long idAdministrador, Long idEjercicio, String nuevoEstado) {
        if (!Ejercicio.ACTIVO.equals(nuevoEstado) && !Ejercicio.DESACTIVADO.equals(nuevoEstado)) {
            throw new DatosInvalidos("ESTADO_INVALIDO", "El estado debe ser Activo o Desactivado.");
        }
        Ejercicio ejercicio = buscar(idEjercicio);
        if (nuevoEstado.equals(ejercicio.estado())) {
            throw new Conflicto("ESTADO_SIN_CAMBIO", "El ejercicio ya está " + nuevoEstado.toLowerCase() + ".");
        }
        boolean activar = Ejercicio.ACTIVO.equals(nuevoEstado);
        ejercicios.guardar(ejercicio.conEstado(nuevoEstado));
        auditoria.registrar(Auditoria.de(activar ? Auditoria.ACTIVACION : Auditoria.DESACTIVACION,
                idEjercicio, idAdministrador, reloj.ahora()));
        return detalleDe(idEjercicio);
    }

    private Ejercicio buscar(Long idEjercicio) {
        return ejercicios.porId(idEjercicio).orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
    }

    private DetalleDeEjercicio detalleDe(Long idEjercicio) {
        return new DetalleDeEjercicio(buscar(idEjercicio), intentos.usoDe(idEjercicio),
                intentos.opcionesUsadas(idEjercicio));
    }
}
