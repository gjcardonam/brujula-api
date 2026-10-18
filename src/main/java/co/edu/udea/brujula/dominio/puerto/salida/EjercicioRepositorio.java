package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;

import java.util.Optional;

public interface EjercicioRepositorio {

    Pagina<Ejercicio> buscar(Long idComponente, boolean soloActivos, int pagina, int tamano);

    Optional<Ejercicio> porId(Long id);

    ComponentesDelBanco conteoPorComponente(boolean soloActivos);

    boolean existeConEnunciado(String enunciado);

    Ejercicio guardar(Ejercicio ejercicio);

    Optional<Long> siguienteParaPractica(Long idActual, Long idComponente, Long idEstudiante);
}
