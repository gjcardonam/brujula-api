package co.edu.udea.brujula.ejercicio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "auditoria_ejercicios")
@Getter @Setter
public class AuditoriaEjercicio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long id;

    @Column(name = "tipo_accion", nullable = false, length = 15)
    private String tipoAccion;

    @Column(name = "fecha_accion", nullable = false)
    private Instant fechaAccion = Instant.now();

    @Column(name = "id_ejercicio", nullable = false)
    private Long idEjercicio;

    @Column(name = "id_usuario_actor", nullable = false)
    private Long idUsuarioActor;
}
