package co.edu.udea.brujula.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "duraciones_simulacro")
@Getter @Setter
public class DuracionSimulacro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_duracion")
    private Long id;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos;
}
