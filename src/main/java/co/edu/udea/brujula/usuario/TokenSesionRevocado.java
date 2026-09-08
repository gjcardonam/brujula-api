package co.edu.udea.brujula.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tokens_sesion_revocados")
@Getter @Setter
public class TokenSesionRevocado {
    @Id
    @Column(length = 64)
    private String jti;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "revocado_en", nullable = false)
    private Instant revocadoEn = Instant.now();

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;
}
