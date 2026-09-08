package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

@Entity
@Table(name = "niveles_dificultad")
public class NivelDificultadEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel_dificultad")
    private Long id;

    @Column(nullable = false, length = 15)
    private String nivel;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }
}
