package co.edu.udea.brujula.dominio.servicio;

import java.time.Duration;
import java.time.Instant;

public final class LimiteDeReintentos {

    private LimiteDeReintentos() {
    }

    public record Practica(long intentos, Instant ultimo) {
    }

    public record Disponibilidad(int restantes, Instant disponibleDesde) {

        public boolean permiteResponder(Instant ahora) {
            return restantes > 0 && (disponibleDesde == null || !ahora.isBefore(disponibleDesde));
        }
    }

    public static Disponibilidad evaluar(Practica practica, int maximo, int horasDeEspera) {
        int restantes = (int) Math.max(0, maximo - practica.intentos());
        Instant desde = restantes > 0 && practica.ultimo() != null && horasDeEspera > 0
                ? practica.ultimo().plus(Duration.ofHours(horasDeEspera))
                : null;
        return new Disponibilidad(restantes, desde);
    }
}
