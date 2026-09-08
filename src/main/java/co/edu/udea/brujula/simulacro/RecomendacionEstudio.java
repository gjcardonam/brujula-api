package co.edu.udea.brujula.simulacro;

import co.edu.udea.brujula.catalogo.Competencia;
import co.edu.udea.brujula.catalogo.Componente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "recomendaciones_estudio")
@Getter @Setter
public class RecomendacionEstudio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion")
    private Long id;

    @Column(name = "mensaje_recomendacion", nullable = false, columnDefinition = "text")
    private String mensaje;

    @Column(name = "porcentaje_aciertos", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeAciertos;

    @Column(name = "orden_prioridad", nullable = false)
    private int ordenPrioridad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_simulacro", nullable = false)
    private Simulacro simulacro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competencia")
    private Competencia competencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_componente")
    private Componente componente;
}
