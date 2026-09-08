package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

@Entity
@Table(name = "parametros_sistema")
public class ParametroSistemaEntidad {

    @Id
    @Column(length = 50)
    private String clave;

    @Column(nullable = false, length = 50)
    private String valor;

    @Column(length = 255)
    private String descripcion;

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
