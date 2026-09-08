package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IntentoRepositorio {

    Intento guardar(Intento intento);

    /** Busca un intento ya confirmado con el mismo token para no duplicarlo (HU-016 CA-02). */
    Optional<Intento> porToken(UUID token);

    long cantidadDeIntentos(Long idEstudiante, Long idEjercicio);

    Map<Long, Long> conteoPorEjercicio(List<Long> idsDeEjercicios);

    UsoDelEjercicio usoDe(Long idEjercicio);

    /** Opciones que ya aparecen en algún intento y por eso no se pueden modificar (HU-021 CA-09). */
    Set<Long> opcionesUsadas(Long idEjercicio);

    boolean yaRespondidoEnSimulacro(Long idSimulacro, Long idEjercicio);

    /**
     * Últimos resultados del estudiante en la misma competencia o componente, del más reciente al
     * más antiguo. Es la ventana con la que se mide el dominio previo (HU-028 CA-01).
     */
    List<Boolean> ultimosResultados(Long idEstudiante, Long idComponente, Long idCompetencia, int limite);

    Pagina<ResumenDeIntento> historialDe(Long idEstudiante, int pagina, int tamano);

    Optional<DetalleDeIntento> detalle(Long idIntento, Long idEstudiante);

    List<DetalleDeIntento> detalleDelSimulacro(Long idSimulacro);

    /** Intentos del simulacro con su ejercicio (sin opciones), para calcular el desempeño por área. */
    List<Intento> delSimulacro(Long idSimulacro);

    long respondidosEn(Long idSimulacro);

    long correctasEn(Long idSimulacro);

    /** Todos los intentos del estudiante, con el ejercicio sin opciones. Alimenta las estadísticas. */
    List<Intento> deEstudiante(Long idEstudiante);
}
