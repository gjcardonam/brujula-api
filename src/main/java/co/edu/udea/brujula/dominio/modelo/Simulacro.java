package co.edu.udea.brujula.dominio.modelo;

import java.time.Duration;
import java.time.Instant;

public class Simulacro {

    public static final String EN_CURSO = "En curso";
    public static final String FINALIZADO = "Finalizado";

    private Long id;
    private final Long idEstudiante;
    private final DuracionSimulacro duracion;
    private final Instant inicio;
    private Instant fin;
    private String estado;
    private Integer tiempoUtilizadoSeg;

    public Simulacro(Long id, Long idEstudiante, DuracionSimulacro duracion, Instant inicio, Instant fin,
                     String estado, Integer tiempoUtilizadoSeg) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.duracion = duracion;
        this.inicio = inicio;
        this.fin = fin;
        this.estado = estado;
        this.tiempoUtilizadoSeg = tiempoUtilizadoSeg;
    }

    public static Simulacro iniciar(Long idEstudiante, DuracionSimulacro duracion, Instant ahora) {
        return new Simulacro(null, idEstudiante, duracion, ahora, null, EN_CURSO, null);
    }

    public boolean enCurso() {
        return EN_CURSO.equals(estado);
    }

    public Instant finPrevisto() {
        return inicio.plus(Duration.ofMinutes(duracion.minutos()));
    }

    public boolean tiempoAgotado(Instant ahora) {
        return !ahora.isBefore(finPrevisto());
    }

    public long segundosRestantes(Instant ahora) {
        if (!enCurso()) return 0;
        return Math.max(0, Duration.between(ahora, finPrevisto()).getSeconds());
    }

    public void finalizar(Instant ahora) {
        if (!enCurso()) return;
        fin = ahora.isAfter(finPrevisto()) ? finPrevisto() : ahora;
        tiempoUtilizadoSeg = (int) Math.max(0, Duration.between(inicio, fin).getSeconds());
        estado = FINALIZADO;
    }

    public Long id() { return id; }
    public void asignarId(Long id) { this.id = id; }
    public Long idEstudiante() { return idEstudiante; }
    public DuracionSimulacro duracion() { return duracion; }
    public Instant inicio() { return inicio; }
    public Instant fin() { return fin; }
    public String estado() { return estado; }
    public Integer tiempoUtilizadoSeg() { return tiempoUtilizadoSeg; }
}
