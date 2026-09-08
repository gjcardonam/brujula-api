package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class UsuarioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Instant creadoEn;

    @Column(name = "intentos_fallidos_login", nullable = false)
    private int intentosFallidos;

    @Column(name = "ultimo_login")
    private Instant ultimoLogin;

    @Column(name = "fecha_bloqueo")
    private Instant fechaBloqueo;

    @Column(nullable = false, length = 10)
    private String estado;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private RolEntidad rol;

    @Column(name = "password_actualizado_en")
    private Instant passwordActualizadoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGoogleSub() { return googleSub; }
    public void setGoogleSub(String googleSub) { this.googleSub = googleSub; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isAceptoTerminos() { return aceptoTerminos; }
    public void setAceptoTerminos(boolean aceptoTerminos) { this.aceptoTerminos = aceptoTerminos; }
    public Instant getFechaAceptacionTerminos() { return fechaAceptacionTerminos; }
    public void setFechaAceptacionTerminos(Instant f) { this.fechaAceptacionTerminos = f; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public Instant getUltimoLogin() { return ultimoLogin; }
    public void setUltimoLogin(Instant ultimoLogin) { this.ultimoLogin = ultimoLogin; }
    public Instant getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(Instant fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public RolEntidad getRol() { return rol; }
    public void setRol(RolEntidad rol) { this.rol = rol; }
    public Instant getPasswordActualizadoEn() { return passwordActualizadoEn; }
    public void setPasswordActualizadoEn(Instant p) { this.passwordActualizadoEn = p; }
}
