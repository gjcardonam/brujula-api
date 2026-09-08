package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

@Entity
@Table(name = "duraciones_simulacro")
public class DuracionSimulacroEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_duracion")
    private Long id;

    @Column(name = "duracion_minutos", nullable = false)
    private int minutos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getMinutos() { return minutos; }
    public void setMinutos(int minutos) { this.minutos = minutos; }
}
