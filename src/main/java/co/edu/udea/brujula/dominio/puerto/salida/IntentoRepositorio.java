package co.edu.udea.brujula.dominio.puerto.salida;

import co.edu.udea.brujula.dominio.modelo.Intento;

import java.util.Optional;
import java.util.UUID;

public interface IntentoRepositorio {

    Intento guardar(Intento intento);

    Optional<Intento> porToken(UUID token);

    long cantidadDeIntentos(Long idEstudiante, Long idEjercicio);
}
