package co.edu.udea.brujula.dominio.modelo;

import java.time.Duration;
import java.time.Instant;

/**
 * Cuenta de la plataforma. Aquí vive la regla de los intentos fallidos de HU-002 CA-08, para no
 * repartirla entre el caso de uso y la base de datos.
 */
public class Usuario {

    public static final String ACTIVO = "Activo";

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String googleSub;
    private String passwordHash;
    private boolean aceptoTerminos;
    private Instant fechaAceptacionTerminos;
    private Instant creadoEn;
    private int intentosFallidos;
    private Instant ultimoLogin;
    private Instant fechaBloqueo;
    private String estado;
    private Rol rol;
    private Instant passwordActualizadoEn;

    public Usuario() {
    }

    /** Cuenta nueva. En el registro normal el rol es Estudiante (HU-001 CA-08). */
    public static Usuario crear(String nombre, String apellido, String email, String googleSub,
                                String passwordHash, Rol rol, Instant ahora) {
        Usuario u = new Usuario();
        u.nombre = nombre.trim();
        u.apellido = apellido.trim();
        u.email = email.toLowerCase();
        u.googleSub = googleSub;
        u.passwordHash = passwordHash;
        u.aceptoTerminos = true;
        u.fechaAceptacionTerminos = ahora;
        u.creadoEn = ahora;
        u.estado = ACTIVO;
        u.rol = rol;
        return u;
    }

    public boolean estaActivo() {
        return ACTIVO.equals(estado);
    }

    public boolean esAdministrador() {
        return rol != null && rol.esAdministrador();
    }

    public boolean estaBloqueado(Instant ahora, int minutosDeBloqueo) {
        return fechaBloqueo != null && ahora.isBefore(fechaBloqueo.plus(Duration.ofMinutes(minutosDeBloqueo)));
    }

    public long minutosDeBloqueoRestantes(Instant ahora, int minutosDeBloqueo) {
        if (fechaBloqueo == null) return 0;
        Duration falta = Duration.between(ahora, fechaBloqueo.plus(Duration.ofMinutes(minutosDeBloqueo)));
        return Math.max(1, falta.toMinutes() + 1);
    }

    /** Se llama cuando ya pasó el bloqueo: vuelve a arrancar el contador (HU-002 CA-08). */
    public void levantarBloqueo() {
        fechaBloqueo = null;
        intentosFallidos = 0;
    }

    /** Suma un intento fallido y bloquea la cuenta si llegó al máximo. Devuelve true si quedó bloqueada. */
    public boolean registrarIngresoFallido(Instant ahora, int maximoDeIntentos) {
        intentosFallidos++;
        if (intentosFallidos >= maximoDeIntentos) {
            fechaBloqueo = ahora;
            return true;
        }
        return false;
    }

    public void registrarIngresoExitoso(Instant ahora) {
        intentosFallidos = 0;
        fechaBloqueo = null;
        ultimoLogin = ahora;
    }

    public void cambiarPassword(String nuevoHash) {
        this.passwordHash = nuevoHash;
    }

    /**
     * Restablecer la contraseña deja sin efecto las sesiones abiertas (HU-003 CA-07): se marca el
     * momento del cambio y los tokens emitidos antes dejan de valer.
     */
    public void restablecerPassword(String nuevoHash, Instant ahora) {
        this.passwordHash = nuevoHash;
        this.passwordActualizadoEn = ahora;
        levantarBloqueo();
    }

    public void actualizarDatosPersonales(String nombre, String apellido) {
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
    }

    public String nombreCompleto() {
        return nombre + " " + apellido;
    }

    public Long id() { return id; }
    public void asignarId(Long id) { this.id = id; }
    public String nombre() { return nombre; }
    public String apellido() { return apellido; }
    public String email() { return email; }
    public String googleSub() { return googleSub; }
    public String passwordHash() { return passwordHash; }
    public boolean aceptoTerminos() { return aceptoTerminos; }
    public Instant fechaAceptacionTerminos() { return fechaAceptacionTerminos; }
    public Instant creadoEn() { return creadoEn; }
    public int intentosFallidos() { return intentosFallidos; }
    public Instant ultimoLogin() { return ultimoLogin; }
    public Instant fechaBloqueo() { return fechaBloqueo; }
    public String estado() { return estado; }
    public Rol rol() { return rol; }
    public Instant passwordActualizadoEn() { return passwordActualizadoEn; }

    /** Solo lo usa el adaptador de persistencia para reconstruir el usuario que viene de la base. */
    public static Usuario reconstruir(Long id, String nombre, String apellido, String email, String googleSub,
                                      String passwordHash, boolean aceptoTerminos, Instant fechaAceptacionTerminos,
                                      Instant creadoEn, int intentosFallidos, Instant ultimoLogin, Instant fechaBloqueo,
                                      String estado, Rol rol, Instant passwordActualizadoEn) {
        Usuario u = new Usuario();
        u.id = id;
        u.nombre = nombre;
        u.apellido = apellido;
        u.email = email;
        u.googleSub = googleSub;
        u.passwordHash = passwordHash;
        u.aceptoTerminos = aceptoTerminos;
        u.fechaAceptacionTerminos = fechaAceptacionTerminos;
        u.creadoEn = creadoEn;
        u.intentosFallidos = intentosFallidos;
        u.ultimoLogin = ultimoLogin;
        u.fechaBloqueo = fechaBloqueo;
        u.estado = estado;
        u.rol = rol;
        u.passwordActualizadoEn = passwordActualizadoEn;
        return u;
    }
}
