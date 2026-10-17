package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "recomendaciones_estudio")
public class RecomendacionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion")
    private Long id;

    @Column(name = "id_simulacro", nullable = false)
    private Long idSimulacro;

    @Column(name = "orden_prioridad", nullable = false)
    private short orden;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_componente")
    private ComponenteEntidad componente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_competencia")
    private CompetenciaEntidad competencia;

    @Column(name = "porcentaje_aciertos", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "mensaje_recomendacion", nullable = false, columnDefinition = "text")
    private String mensaje;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdSimulacro() { return idSimulacro; }
    public void setIdSimulacro(Long idSimulacro) { this.idSimulacro = idSimulacro; }
    public short getOrden() { return orden; }
    public void setOrden(short orden) { this.orden = orden; }
    public ComponenteEntidad getComponente() { return componente; }
    public void setComponente(ComponenteEntidad componente) { this.componente = componente; }
    public CompetenciaEntidad getCompetencia() { return competencia; }
    public void setCompetencia(CompetenciaEntidad competencia) { this.competencia = competencia; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
