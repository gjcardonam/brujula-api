package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "simulacros")
public class SimulacroEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulacro")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_duracion", nullable = false)
    private DuracionSimulacroEntidad duracion;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant inicio;

    @Column(name = "fecha_fin")
    private Instant fin;

    @Column(nullable = false, length = 15)
    private String estado;

    @Column(name = "tiempo_utilizado_seg")
    private Integer tiempoUtilizadoSeg;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public DuracionSimulacroEntidad getDuracion() { return duracion; }
    public void setDuracion(DuracionSimulacroEntidad duracion) { this.duracion = duracion; }
    public Instant getInicio() { return inicio; }
    public void setInicio(Instant inicio) { this.inicio = inicio; }
    public Instant getFin() { return fin; }
    public void setFin(Instant fin) { this.fin = fin; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getTiempoUtilizadoSeg() { return tiempoUtilizadoSeg; }
    public void setTiempoUtilizadoSeg(Integer t) { this.tiempoUtilizadoSeg = t; }
}
