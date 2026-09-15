package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ejercicios")
public class EjercicioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejercicio")
    private Long id;

    @Generated
    @Column(name = "numero", insertable = false, updatable = false)
    private Integer numero;

    @Column(nullable = false, columnDefinition = "text")
    private String enunciado;

    @Column(name = "imagen_enunciado", length = 255)
    private String imagen;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_componente", nullable = false)
    private ComponenteEntidad componente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_competencia", nullable = false)
    private CompetenciaEntidad competencia;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_nivel_dificultad", nullable = false)
    private NivelDificultadEntidad nivel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_creador", nullable = false)
    private UsuarioEntidad creador;

    @Column(nullable = false, length = 15)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @OneToMany(mappedBy = "ejercicio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<OpcionEntidad> opciones = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumero() { return numero; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public ComponenteEntidad getComponente() { return componente; }
    public void setComponente(ComponenteEntidad componente) { this.componente = componente; }
    public CompetenciaEntidad getCompetencia() { return competencia; }
    public void setCompetencia(CompetenciaEntidad competencia) { this.competencia = competencia; }
    public NivelDificultadEntidad getNivel() { return nivel; }
    public void setNivel(NivelDificultadEntidad nivel) { this.nivel = nivel; }
    public UsuarioEntidad getCreador() { return creador; }
    public void setCreador(UsuarioEntidad creador) { this.creador = creador; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public List<OpcionEntidad> getOpciones() { return opciones; }
}
