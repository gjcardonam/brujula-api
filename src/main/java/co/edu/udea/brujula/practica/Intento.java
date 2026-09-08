package co.edu.udea.brujula.practica;

import co.edu.udea.brujula.catalogo.TipoError;
import co.edu.udea.brujula.ejercicio.Ejercicio;
import co.edu.udea.brujula.ejercicio.OpcionRespuesta;
import co.edu.udea.brujula.simulacro.Simulacro;
import co.edu.udea.brujula.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "intentos")
@Getter @Setter
public class Intento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private Instant fechaHora = Instant.now();

    @Column(name = "es_correcto", nullable = false)
    private boolean esCorrecto;

    @Column(name = "nivel_confianza", nullable = false)
    private int nivelConfianza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_error")
    private TipoError tipoError;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejercicio", nullable = false)
    private Ejercicio ejercicio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opcion_seleccionada", nullable = false)
    private OpcionRespuesta opcionSeleccionada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_simulacro")
    private Simulacro simulacro;

    @Column(name = "token_idempotencia")
    private UUID tokenIdempotencia;
}
