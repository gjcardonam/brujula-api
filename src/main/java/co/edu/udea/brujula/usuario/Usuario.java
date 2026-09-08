package co.edu.udea.brujula.usuario;

import co.edu.udea.brujula.catalogo.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
@Getter @Setter
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 30)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(name = "google_sub", length = 255)
    private String googleSub;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "acepto_terminos", nullable = false)
    private boolean aceptoTerminos;

    @Column(name = "fecha_aceptacion_terminos")
    private Instant fechaAceptacionTerminos;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn = Instant.now();

    @Column(name = "intentos_fallidos_login", nullable = false)
    private int intentosFallidosLogin;

    @Column(name = "ultimo_login")
    private Instant ultimoLogin;

    @Column(name = "fecha_bloqueo")
    private Instant fechaBloqueo;

    @Column(nullable = false, length = 10)
    private String estado = "Activo";

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "password_actualizado_en")
    private Instant passwordActualizadoEn;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
