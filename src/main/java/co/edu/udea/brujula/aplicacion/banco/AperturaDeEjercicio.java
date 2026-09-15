package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.excepcion.RecursoNoDisponible;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.AbrirEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.IntentoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AperturaDeEjercicio implements AbrirEjercicio {

    private final EjercicioRepositorio ejercicios;
    private final IntentoRepositorio intentos;

    public AperturaDeEjercicio(EjercicioRepositorio ejercicios, IntentoRepositorio intentos) {
        this.ejercicios = ejercicios;
        this.intentos = intentos;
    }

    @Override
    @Transactional(readOnly = true)
    public ParaPracticar abrir(Long idEstudiante, Long idEjercicio) {
        Ejercicio ejercicio = ejercicios.porId(idEjercicio)
                .orElseThrow(() -> new NoEncontrado("El ejercicio no existe."));
        if (!ejercicio.estaActivo()) {
            throw new RecursoNoDisponible("Este ejercicio ya no está disponible.");
        }
        return new ParaPracticar(ejercicio, intentos.cantidadDeIntentos(idEstudiante, idEjercicio));
    }
}
