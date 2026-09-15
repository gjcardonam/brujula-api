package co.edu.udea.brujula.infraestructura.salida.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    @Column(name = "password_actualizado_en", nullable = false)
    private Instant passwordActualizadoEn;

    @Column(name = "acepto_terminos", nullable = false)
    private boolean aceptoTerminos;

    @Column(name = "terminos_aceptados_en", nullable = false)
    private Instant terminosAceptadosEn;

    @Column(name = "intentos_fallidos_login", nullable = false)
    private short intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private Instant bloqueadoHasta;

    @Column(name = "ultimo_login_en")
    private Instant ultimoLoginEn;

    @Column(nullable = false, length = 10)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private RolEntidad rol;

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
    public Instant getPasswordActualizadoEn() { return passwordActualizadoEn; }
    public void setPasswordActualizadoEn(Instant passwordActualizadoEn) { this.passwordActualizadoEn = passwordActualizadoEn; }
    public boolean isAceptoTerminos() { return aceptoTerminos; }
    public void setAceptoTerminos(boolean aceptoTerminos) { this.aceptoTerminos = aceptoTerminos; }
    public Instant getTerminosAceptadosEn() { return terminosAceptadosEn; }
    public void setTerminosAceptadosEn(Instant terminosAceptadosEn) { this.terminosAceptadosEn = terminosAceptadosEn; }
    public short getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(short intentosFallidos) { this.intentosFallidos = intentosFallidos; }
    public Instant getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(Instant bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
    public Instant getUltimoLoginEn() { return ultimoLoginEn; }
    public void setUltimoLoginEn(Instant ultimoLoginEn) { this.ultimoLoginEn = ultimoLoginEn; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public RolEntidad getRol() { return rol; }
    public void setRol(RolEntidad rol) { this.rol = rol; }
}
