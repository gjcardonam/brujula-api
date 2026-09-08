package co.edu.udea.brujula.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "componentes")
@Getter @Setter
public class Componente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_componente")
    private Long id;

    @Column(name = "nombre_componente", nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 15)
    private String estado;
}
