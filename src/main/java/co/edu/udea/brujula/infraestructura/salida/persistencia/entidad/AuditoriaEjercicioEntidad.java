package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "auditoria_ejercicios")
public class AuditoriaEjercicioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long id;

    @Column(name = "tipo_accion", nullable = false, length = 15)
    private String accion;

    @Column(name = "fecha_accion", nullable = false)
    private Instant fecha;

    @Column(name = "id_ejercicio", nullable = false)
    private Long idEjercicio;

    @Column(name = "id_usuario_actor", nullable = false)
    private Long idActor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public Long getIdEjercicio() { return idEjercicio; }
    public void setIdEjercicio(Long idEjercicio) { this.idEjercicio = idEjercicio; }
    public Long getIdActor() { return idActor; }
    public void setIdActor(Long idActor) { this.idActor = idActor; }
}
