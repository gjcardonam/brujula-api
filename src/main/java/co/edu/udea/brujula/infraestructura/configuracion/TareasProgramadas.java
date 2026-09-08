package co.edu.udea.brujula.infraestructura.configuracion;

import co.edu.udea.brujula.dominio.puerto.entrada.GestionarSesion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** La tabla de sesiones revocadas crece con cada cierre de sesión; se limpia lo que ya venció. */
@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final GestionarSesion sesiones;

    public TareasProgramadas(GestionarSesion sesiones) {
        this.sesiones = sesiones;
    }

    @Scheduled(fixedDelayString = "PT1H")
    public void limpiarSesionesRevocadas() {
        int borradas = sesiones.limpiarRevocadasVencidas();
        if (borradas > 0) {
            log.info("Se eliminaron {} tokens revocados que ya habían expirado", borradas);
        }
    }
}
