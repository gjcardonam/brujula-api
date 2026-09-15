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

    @Column(name = "token_idempotencia", nullable = false)
    private UUID tokenIdempotencia;

    @Column(name = "respondido_en", nullable = false)
    private Instant respondidoEn;

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
    public UUID getTokenIdempotencia() { return tokenIdempotencia; }
    public void setTokenIdempotencia(UUID tokenIdempotencia) { this.tokenIdempotencia = tokenIdempotencia; }
    public Instant getRespondidoEn() { return respondidoEn; }
    public void setRespondidoEn(Instant respondidoEn) { this.respondidoEn = respondidoEn; }
}
