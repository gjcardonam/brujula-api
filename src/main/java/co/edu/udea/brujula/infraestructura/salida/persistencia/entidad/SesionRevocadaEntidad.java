package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tokens_sesion_revocados")
public class SesionRevocadaEntidad {

    @Id
    @Column(length = 64)
    private String jti;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "revocado_en", nullable = false)
    private Instant revocadaEn;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public Instant getRevocadaEn() { return revocadaEn; }
    public void setRevocadaEn(Instant revocadaEn) { this.revocadaEn = revocadaEn; }
    public Instant getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Instant expiraEn) { this.expiraEn = expiraEn; }
}
