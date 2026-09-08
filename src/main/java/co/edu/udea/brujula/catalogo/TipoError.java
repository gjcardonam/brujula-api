package co.edu.udea.brujula.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipos_error")
@Getter @Setter
public class TipoError {
    public static final String COGNITIVO = "Error cognitivo";
    public static final String HABITO = "Error de hábito";
    public static final String ANSIEDAD = "Error de ansiedad";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_error")
    private Long id;

    @Column(name = "nombre_tipo_error", nullable = false, length = 30)
    private String nombre;
}
