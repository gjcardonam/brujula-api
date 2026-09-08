package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;

/** Traza de lo que hace un administrador sobre el banco (creación, edición, activación, desactivación). */
public record Auditoria(Long id, String accion, Instant fecha, Long idEjercicio, Long idActor) {

    public static final String CREACION = "Creación";
    public static final String EDICION = "Edición";
    public static final String ACTIVACION = "Activación";
    public static final String DESACTIVACION = "Desactivación";

    public static Auditoria de(String accion, Long idEjercicio, Long idActor, Instant ahora) {
        return new Auditoria(null, accion, ahora, idEjercicio, idActor);
    }
}
