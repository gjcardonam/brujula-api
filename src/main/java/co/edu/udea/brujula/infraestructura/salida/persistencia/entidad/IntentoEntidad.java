package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intentos")
public class IntentoEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora;

    @Column(name = "es_correcto", nullable = false)
    private boolean correcto;

    @Column(name = "nivel_confianza", nullable = false)
    private int nivelConfianza;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_error")
    private TipoErrorEntidad tipoError;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejercicio", nullable = false)
    private EjercicioEntidad ejercicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opcion_seleccionada", nullable = false)
    private OpcionEntidad opcionSeleccionada;

    @Column(name = "id_simulacro")
    private Long idSimulacro;

    @Column(name = "token_idempotencia")
    private UUID tokenIdempotencia;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getFechaHora() { return fechaHora; }
    public void setFechaHora(Instant fechaHora) { this.fechaHora = fechaHora; }
    public boolean isCorrecto() { return correcto; }
    public void setCorrecto(boolean correcto) { this.correcto = correcto; }
    public int getNivelConfianza() { return nivelConfianza; }
    public void setNivelConfianza(int nivelConfianza) { this.nivelConfianza = nivelConfianza; }
    public TipoErrorEntidad getTipoError() { return tipoError; }
    public void setTipoError(TipoErrorEntidad tipoError) { this.tipoError = tipoError; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public EjercicioEntidad getEjercicio() { return ejercicio; }
    public void setEjercicio(EjercicioEntidad ejercicio) { this.ejercicio = ejercicio; }
    public OpcionEntidad getOpcionSeleccionada() { return opcionSeleccionada; }
    public void setOpcionSeleccionada(OpcionEntidad o) { this.opcionSeleccionada = o; }
    public Long getIdSimulacro() { return idSimulacro; }
    public void setIdSimulacro(Long idSimulacro) { this.idSimulacro = idSimulacro; }
    public UUID getTokenIdempotencia() { return tokenIdempotencia; }
    public void setTokenIdempotencia(UUID token) { this.tokenIdempotencia = token; }
}
