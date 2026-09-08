package co.edu.udea.brujula.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "niveles_dificultad")
@Getter @Setter
public class NivelDificultad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel_dificultad")
    private Long id;

    @Column(nullable = false, length = 15)
    private String nivel;
}
