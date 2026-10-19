package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeIntento;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface IntentoRepositorio {

    Intento guardar(Intento intento);

    Optional<Intento> porToken(UUID token);

    long cantidadDeIntentos(Long idEstudiante, Long idEjercicio);

    UsoDelEjercicio usoDe(Long idEjercicio);

    Set<Long> opcionesUsadas(Long idEjercicio);

    List<Boolean> ultimosResultados(Long idEstudiante, Long idComponente, Long idCompetencia, int limite);

    Pagina<ResumenDeIntento> historialDe(Long idEstudiante, int pagina, int tamano);

    Optional<DetalleDeIntento> detalle(Long idIntento, Long idEstudiante);
}
