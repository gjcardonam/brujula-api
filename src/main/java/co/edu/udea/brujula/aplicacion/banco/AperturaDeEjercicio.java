package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.excepcion.RecursoNoDisponible;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.AbrirEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.dominio.servicio.LimiteDeReintentos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AperturaDeEjercicio implements AbrirEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final IntentoRepositorio intentos;
    private final ParametrosDelSistema parametros;

    public AperturaDeEjercicio(EjercicioRepositorio ejercicios, IntentoRepositorio intentos,
                               ParametrosDelSistema parametros) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
        this.parametros = parametros;
    }

    @Override
    @Transactional(readOnly = true)
    public ParaPracticar abrir(Long idEstudiante, Long idEjercicio) {
        Ejercicio ejercicio = ejercicios.porId(idEjercicio)
                .orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
        if (!ejercicio.estaActivo()) {
            throw new RecursoNoDisponible("Este ejercicio ya no está disponible.");
        }
        LimiteDeReintentos.Disponibilidad disponibilidad = LimiteDeReintentos.evaluar(
                intentos.practicaDe(idEstudiante, idEjercicio),
                parametros.entero(ParametrosDelSistema.MAX_INTENTOS_PRACTICA, 2),
                parametros.entero(ParametrosDelSistema.HORAS_ESPERA_REINTENTO, 24));
        return new ParaPracticar(ejercicio, intentos.cantidadDeIntentos(idEstudiante, idEjercicio),
                disponibilidad.restantes(), disponibilidad.disponibleDesde());
    }
}
