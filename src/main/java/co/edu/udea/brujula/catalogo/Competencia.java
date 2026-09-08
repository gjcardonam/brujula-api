package co.edu.udea.brujula.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "competencias")
@Getter @Setter
public class Competencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_competencia")
    private Long id;

    @Column(name = "nombre_competencia", nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 15)
    private String estado;
}
