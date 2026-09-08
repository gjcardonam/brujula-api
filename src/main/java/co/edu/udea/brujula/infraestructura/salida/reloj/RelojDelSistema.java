package co.edu.udea.brujula.infraestructura.salida.reloj;

import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RelojDelSistema implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
