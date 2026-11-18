package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "duraciones_simulacro")
public class DuracionSimulacroEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_duracion")
    private Long id;

    @Column(name = "duracion_minutos", nullable = false)
    private short minutos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public short getMinutos() { return minutos; }
    public void setMinutos(short minutos) { this.minutos = minutos; }
}
