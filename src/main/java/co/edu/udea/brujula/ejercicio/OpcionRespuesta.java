package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.catalogo.TipoError;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "opciones_respuesta")
@Getter @Setter
public class OpcionRespuesta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Long id;

    @Column(name = "descripcion_opcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "imagen_opcion", length = 255)
    private String imagen;

    @Column(name = "es_correcta", nullable = false)
    private boolean esCorrecta;

    @Column(nullable = false, columnDefinition = "text")
    private String retroalimentacion;

    /** Tipo de error que representa el distractor (nulo en la opción correcta). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_error")
    private TipoError tipoError;

    @Column(name = "orden_opcion", nullable = false)
    private int orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ejercicio", nullable = false)
    private Ejercicio ejercicio;

    /** Letra visible: A, B, C... según el orden. */
    public String getLetra() {
        return String.valueOf((char) ('A' + Math.max(0, orden - 1)));
    }
}
