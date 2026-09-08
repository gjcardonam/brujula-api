package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "recomendaciones_estudio")
public class RecomendacionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion")
    private Long id;

    @Column(name = "mensaje_recomendacion", nullable = false, columnDefinition = "text")
    private String mensaje;

    @Column(name = "porcentaje_aciertos", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "orden_prioridad", nullable = false)
    private int orden;

    @Column(name = "id_simulacro", nullable = false)
    private Long idSimulacro;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_competencia")
    private CompetenciaEntidad competencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_componente")
    private ComponenteEntidad componente;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
    public Long getIdSimulacro() { return idSimulacro; }
    public void setIdSimulacro(Long idSimulacro) { this.idSimulacro = idSimulacro; }
    public CompetenciaEntidad getCompetencia() { return competencia; }
    public void setCompetencia(CompetenciaEntidad competencia) { this.competencia = competencia; }
    public ComponenteEntidad getComponente() { return componente; }
    public void setComponente(ComponenteEntidad componente) { this.componente = componente; }
}
