package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;
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

    /** Lo asigna una secuencia de la base de datos (HU-020 CA-11). */
    @Generated
    @Column(name = "numero", insertable = false, updatable = false)
    private Integer numero;

    @Column(nullable = false, columnDefinition = "text")
    private String enunciado;

    @Column(name = "imagen_enunciado", length = 255)
    private String imagen;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_nivel_dificultad", nullable = false)
    private NivelDificultadEntidad nivel;

    @Column(nullable = false, length = 15)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_competencia", nullable = false)
    private CompetenciaEntidad competencia;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_componente", nullable = false)
    private ComponenteEntidad componente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_creador", nullable = false)
    private UsuarioEntidad creador;

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
    public NivelDificultadEntidad getNivel() { return nivel; }
    public void setNivel(NivelDificultadEntidad nivel) { this.nivel = nivel; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public CompetenciaEntidad getCompetencia() { return competencia; }
    public void setCompetencia(CompetenciaEntidad competencia) { this.competencia = competencia; }
    public ComponenteEntidad getComponente() { return componente; }
    public void setComponente(ComponenteEntidad componente) { this.componente = componente; }
    public UsuarioEntidad getCreador() { return creador; }
    public void setCreador(UsuarioEntidad creador) { this.creador = creador; }
    public List<OpcionEntidad> getOpciones() { return opciones; }
}
