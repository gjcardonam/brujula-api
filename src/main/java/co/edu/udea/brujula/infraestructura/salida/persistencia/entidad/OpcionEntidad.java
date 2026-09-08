package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

@Entity
@Table(name = "opciones_respuesta")
public class OpcionEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Long id;

    @Column(name = "descripcion_opcion", columnDefinition = "text")
    private String texto;

    @Column(name = "imagen_opcion", length = 255)
    private String imagen;

    @Column(name = "es_correcta", nullable = false)
    private boolean correcta;

    @Column(nullable = false, columnDefinition = "text")
    private String retroalimentacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_error")
    private TipoErrorEntidad tipoError;

    @Column(name = "orden_opcion", nullable = false)
    private int orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejercicio", nullable = false)
    private EjercicioEntidad ejercicio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public boolean isCorrecta() { return correcta; }
    public void setCorrecta(boolean correcta) { this.correcta = correcta; }
    public String getRetroalimentacion() { return retroalimentacion; }
    public void setRetroalimentacion(String retroalimentacion) { this.retroalimentacion = retroalimentacion; }
    public TipoErrorEntidad getTipoError() { return tipoError; }
    public void setTipoError(TipoErrorEntidad tipoError) { this.tipoError = tipoError; }
    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
    public EjercicioEntidad getEjercicio() { return ejercicio; }
    public void setEjercicio(EjercicioEntidad ejercicio) { this.ejercicio = ejercicio; }
}
