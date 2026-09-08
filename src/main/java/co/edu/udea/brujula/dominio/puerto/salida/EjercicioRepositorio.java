package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;

import java.util.Optional;

public interface EjercicioRepositorio {

    /** Los ejercicios de la página llegan sin opciones: el banco solo muestra la ficha. */
    Pagina<Ejercicio> buscar(Long idComponente, boolean soloActivos, int pagina, int tamano);

    Optional<Ejercicio> porId(Long id);

    ComponentesDelBanco conteoPorComponente(boolean soloActivos);

    boolean existeOtroConEnunciado(String enunciado, Long idExcluido);

    long cantidadDeActivos();

    Ejercicio guardar(Ejercicio ejercicio);

    /** Otro ejercicio activo para seguir practicando, preferiblemente uno que no haya intentado. */
    Optional<Long> siguienteParaPractica(Long idActual, Long idComponente, Long idEstudiante);

    /** Ejercicio del simulacro que aún no ha respondido, en un orden estable para ese simulacro. */
    Optional<Long> siguienteParaSimulacro(Long idSimulacro);
}
