package co.edu.udea.brujula.ejercicio;

import co.edu.udea.brujula.catalogo.Competencia;
import co.edu.udea.brujula.catalogo.Componente;
import co.edu.udea.brujula.catalogo.NivelDificultad;
import co.edu.udea.brujula.usuario.Usuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ejercicios")
@Getter @Setter
public class Ejercicio {
    public static final String ACTIVO = "Activo";
    public static final String DESACTIVADO = "Desactivado";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejercicio")
    private Long id;

    /** Número visible, autogenerado por la BD (HU-020 CA-11). */
    @Generated
    @Column(name = "numero", insertable = false, updatable = false)
    private Integer numero;

    @Column(nullable = false, columnDefinition = "text")
    private String enunciado;

    @Column(name = "imagen_enunciado", length = 255)
    private String imagenEnunciado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_nivel_dificultad", nullable = false)
    private NivelDificultad nivelDificultad;

    @Column(nullable = false, length = 15)
    private String estado = ACTIVO;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competencia", nullable = false)
    private Competencia competencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_componente", nullable = false)
    private Componente componente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_creador", nullable = false)
    private Usuario creador;

    @OneToMany(mappedBy = "ejercicio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<OpcionRespuesta> opciones = new ArrayList<>();

    public boolean estaActivo() {
        return ACTIVO.equals(estado);
    }

    public OpcionRespuesta opcionCorrecta() {
        return opciones.stream().filter(OpcionRespuesta::isEsCorrecta).findFirst().orElse(null);
    }
}
