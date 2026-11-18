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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intentos")
public class IntentoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejercicio", nullable = false)
    private EjercicioEntidad ejercicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opcion_seleccionada", nullable = false)
    private OpcionEntidad opcionSeleccionada;

    @Column(name = "es_correcto", nullable = false)
    private boolean correcto;

    @Column(name = "nivel_confianza", nullable = false)
    private short nivelConfianza;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_error")
    private TipoErrorEntidad tipoError;

    @Column(name = "token_idempotencia", nullable = false)
    private UUID tokenIdempotencia;

    @Column(name = "respondido_en", nullable = false)
    private Instant respondidoEn;

    @Column(name = "tiempo_respuesta_ms")
    private Integer tiempoRespuestaMs;

    @Column(name = "id_simulacro")
    private Long idSimulacro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_componente", nullable = false)
    private ComponenteEntidad componente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competencia", nullable = false)
    private CompetenciaEntidad competencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nivel_dificultad", nullable = false)
    private NivelDificultadEntidad nivel;

    @Column(name = "enunciado", nullable = false)
    private String enunciado;

    @Column(name = "imagen_enunciado")
    private String imagenEnunciado;

    @Column(name = "retroalimentacion")
    private String retroalimentacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public EjercicioEntidad getEjercicio() { return ejercicio; }
    public void setEjercicio(EjercicioEntidad ejercicio) { this.ejercicio = ejercicio; }
    public OpcionEntidad getOpcionSeleccionada() { return opcionSeleccionada; }
    public void setOpcionSeleccionada(OpcionEntidad opcionSeleccionada) { this.opcionSeleccionada = opcionSeleccionada; }
    public boolean isCorrecto() { return correcto; }
    public void setCorrecto(boolean correcto) { this.correcto = correcto; }
    public short getNivelConfianza() { return nivelConfianza; }
    public void setNivelConfianza(short nivelConfianza) { this.nivelConfianza = nivelConfianza; }
    public TipoErrorEntidad getTipoError() { return tipoError; }
    public void setTipoError(TipoErrorEntidad tipoError) { this.tipoError = tipoError; }
    public UUID getTokenIdempotencia() { return tokenIdempotencia; }
    public void setTokenIdempotencia(UUID tokenIdempotencia) { this.tokenIdempotencia = tokenIdempotencia; }
    public Instant getRespondidoEn() { return respondidoEn; }
    public void setRespondidoEn(Instant respondidoEn) { this.respondidoEn = respondidoEn; }
    public Integer getTiempoRespuestaMs() { return tiempoRespuestaMs; }
    public void setTiempoRespuestaMs(Integer tiempoRespuestaMs) { this.tiempoRespuestaMs = tiempoRespuestaMs; }

    public Long getIdSimulacro() { return idSimulacro; }
    public void setIdSimulacro(Long idSimulacro) { this.idSimulacro = idSimulacro; }

    public ComponenteEntidad getComponente() { return componente; }
    public void setComponente(ComponenteEntidad componente) { this.componente = componente; }
    public CompetenciaEntidad getCompetencia() { return competencia; }
    public void setCompetencia(CompetenciaEntidad competencia) { this.competencia = competencia; }
    public NivelDificultadEntidad getNivel() { return nivel; }
    public void setNivel(NivelDificultadEntidad nivel) { this.nivel = nivel; }
    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }
    public String getImagenEnunciado() { return imagenEnunciado; }
    public void setImagenEnunciado(String imagenEnunciado) { this.imagenEnunciado = imagenEnunciado; }
    public String getRetroalimentacion() { return retroalimentacion; }
    public void setRetroalimentacion(String retroalimentacion) { this.retroalimentacion = retroalimentacion; }
}
