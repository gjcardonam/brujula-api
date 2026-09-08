package co.edu.udea.brujula.dominio.modelo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class Ejercicio {

    public static final String ACTIVO = "Activo";
    public static final String DESACTIVADO = "Desactivado";

    private Long id;
    private Integer numero;
    private String enunciado;
    private String imagen;
    private NivelDificultad nivel;
    private Componente componente;
    private Competencia competencia;
    private String estado;
    private Instant creadoEn;
    private Long idCreador;
    private String nombreCreador;
    private List<Opcion> opciones;

    public Ejercicio(Long id, Integer numero, String enunciado, String imagen, NivelDificultad nivel,
                     Componente componente, Competencia competencia, String estado, Instant creadoEn,
                     Long idCreador, String nombreCreador, List<Opcion> opciones) {
        this.id = id;
        this.numero = numero;
        this.enunciado = enunciado;
        this.imagen = imagen;
        this.nivel = nivel;
        this.componente = componente;
        this.competencia = competencia;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.idCreador = idCreador;
        this.nombreCreador = nombreCreador;
        this.opciones = opciones == null ? List.of() : List.copyOf(opciones);
    }

    public boolean estaActivo() {
        return ACTIVO.equals(estado);
    }

    public Optional<Opcion> opcionCorrecta() {
        return opciones.stream().filter(Opcion::correcta).findFirst();
    }

    public Optional<Opcion> opcion(Long idOpcion) {
        return opciones.stream().filter(o -> o.id().equals(idOpcion)).findFirst();
    }

    public void activar() {
        estado = ACTIVO;
    }

    public void desactivar() {
        estado = DESACTIVADO;
    }

    public void actualizarContenido(String enunciado, String imagen, Componente componente,
                                    Competencia competencia, NivelDificultad nivel, List<Opcion> opciones) {
        this.enunciado = enunciado.trim();
        this.imagen = imagen;
        this.componente = componente;
        this.competencia = competencia;
        this.nivel = nivel;
        this.opciones = List.copyOf(opciones);
    }

    public Long id() { return id; }
    public void asignarId(Long id) { this.id = id; }
    public Integer numero() { return numero; }
    public String enunciado() { return enunciado; }
    public String imagen() { return imagen; }
    public NivelDificultad nivel() { return nivel; }
    public Componente componente() { return componente; }
    public Competencia competencia() { return competencia; }
    public String estado() { return estado; }
    public Instant creadoEn() { return creadoEn; }
    public Long idCreador() { return idCreador; }
    public String nombreCreador() { return nombreCreador; }
    public List<Opcion> opciones() { return opciones; }
}
