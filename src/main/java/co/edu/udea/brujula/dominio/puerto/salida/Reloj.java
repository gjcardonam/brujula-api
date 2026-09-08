package co.edu.udea.brujula.dominio.puerto.salida;

import java.time.Instant;

/** Se inyecta la hora en vez de llamar a Instant.now() para poder probar el temporizador del simulacro. */
public interface Reloj {
    Instant ahora();
}
