package co.edu.udea.brujula.infraestructura.configuracion;

import co.edu.udea.brujula.dominio.puerto.entrada.DepurarSesionesRevocadas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final DepurarSesionesRevocadas depuracion;

    public TareasProgramadas(DepurarSesionesRevocadas depuracion) {
        this.depuracion = depuracion;
    }

    @Scheduled(fixedDelayString = "PT1H")
    public void limpiarSesionesRevocadas() {
        int borradas = depuracion.depurar();
        if (borradas > 0) {
            log.info("Se eliminaron {} tokens revocados que ya habían expirado", borradas);
        }
    }
}
