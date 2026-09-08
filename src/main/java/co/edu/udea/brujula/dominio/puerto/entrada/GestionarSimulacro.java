package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResultadoDeSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.SiguienteDelSimulacro;

import java.util.Optional;

/**
 * HU-013 a HU-018 y HU-026. Son operaciones distintas pero giran alrededor del mismo simulacro y
 * comparten la regla de cerrarlo cuando se acaba el tiempo, por eso van en un solo puerto.
 */
public interface GestionarSimulacro {

    EstadoDelSimulacro iniciar(Long idEstudiante, Long idDuracion);

    Optional<EstadoDelSimulacro> enCurso(Long idEstudiante);

    EstadoDelSimulacro estado(Long idEstudiante, Long idSimulacro);

    SiguienteDelSimulacro siguienteEjercicio(Long idEstudiante, Long idSimulacro);

    ResultadoDeSimulacro finalizar(Long idEstudiante, Long idSimulacro);

    ResultadoDeSimulacro resultado(Long idEstudiante, Long idSimulacro);

    Pagina<ResumenDeSimulacro> historial(Long idEstudiante, int pagina);
}
