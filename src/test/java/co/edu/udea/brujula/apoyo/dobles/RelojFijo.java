package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.puerto.salida.Reloj;

import java.time.Duration;
import java.time.Instant;

/** Reloj que se puede adelantar a voluntad; así se prueban los bloqueos y los vencimientos. */
public class RelojFijo implements Reloj {

    private Instant ahora = Instant.parse("2026-09-08T14:00:00Z");

    @Override
    public Instant ahora() {
        return ahora;
    }

    public void avanzar(Duration tiempo) {
        ahora = ahora.plus(tiempo);
    }
}
