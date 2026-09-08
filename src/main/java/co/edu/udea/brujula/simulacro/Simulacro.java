package co.edu.udea.brujula.simulacro;

import co.edu.udea.brujula.catalogo.DuracionSimulacro;
import co.edu.udea.brujula.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "simulacros")
@Getter @Setter
public class Simulacro {
    public static final String EN_CURSO = "En curso";
    public static final String FINALIZADO = "Finalizado";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulacro")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_duracion", nullable = false)
    private DuracionSimulacro duracion;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio = Instant.now();

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    @Column(nullable = false, length = 15)
    private String estado = EN_CURSO;

    @Column(name = "tiempo_utilizado_seg")
    private Integer tiempoUtilizadoSeg;

    public boolean enCurso() {
        return EN_CURSO.equals(estado);
    }

    public Instant finPrevisto() {
        return fechaInicio.plus(Duration.ofMinutes(duracion.getDuracionMinutos()));
    }

    public boolean tiempoAgotado(Instant ahora) {
        return !ahora.isBefore(finPrevisto());
    }

    public long segundosRestantes(Instant ahora) {
        return Math.max(0, Duration.between(ahora, finPrevisto()).getSeconds());
    }
}
