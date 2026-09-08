package co.edu.udea.brujula.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "parametros_sistema")
@Getter @Setter
public class ParametroSistema {
    @Id
    @Column(length = 50)
    private String clave;

    @Column(nullable = false, length = 50)
    private String valor;

    @Column(length = 255)
    private String descripcion;
}
