package co.edu.udea.brujula.dominio.modelo;

import java.time.Duration;
import java.time.Instant;

public class Usuario {

    public static final String ACTIVO = "Activo";

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String googleSub;
    private String passwordHash;
    private Instant passwordActualizadoEn;
    private boolean aceptoTerminos;
    private Instant terminosAceptadosEn;
    private Instant creadoEn;
    private int intentosFallidos;
    private Instant bloqueadoHasta;
    private Instant ultimoLoginEn;
    private String estado;
    private Rol rol;

    private Usuario() {
    }

    public static Usuario crear(String nombre, String apellido, String email, String googleSub,
                                String passwordHash, Rol rol, Instant ahora) {
        Usuario usuario = new Usuario();
        usuario.nombre = nombre.trim();
        usuario.apellido = apellido.trim();
        usuario.email = email.trim().toLowerCase();
        usuario.googleSub = googleSub;
        usuario.passwordHash = passwordHash;
        usuario.passwordActualizadoEn = ahora;
        usuario.aceptoTerminos = true;
        usuario.terminosAceptadosEn = ahora;
        usuario.creadoEn = ahora;
        usuario.estado = ACTIVO;
        usuario.rol = rol;
        return usuario;
    }

    public static Usuario reconstruir(Long id, String nombre, String apellido, String email, String googleSub,
                                      String passwordHash, Instant passwordActualizadoEn, boolean aceptoTerminos,
                                      Instant terminosAceptadosEn, Instant creadoEn, int intentosFallidos,
                                      Instant bloqueadoHasta, Instant ultimoLoginEn, String estado, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.id = id;
        usuario.nombre = nombre;
        usuario.apellido = apellido;
        usuario.email = email;
        usuario.googleSub = googleSub;
        usuario.passwordHash = passwordHash;
        usuario.passwordActualizadoEn = passwordActualizadoEn;
        usuario.aceptoTerminos = aceptoTerminos;
        usuario.terminosAceptadosEn = terminosAceptadosEn;
        usuario.creadoEn = creadoEn;
        usuario.intentosFallidos = intentosFallidos;
        usuario.bloqueadoHasta = bloqueadoHasta;
        usuario.ultimoLoginEn = ultimoLoginEn;
        usuario.estado = estado;
        usuario.rol = rol;
        return usuario;
    }

    public boolean estaActivo() {
        return ACTIVO.equals(estado);
    }

    public boolean esAdministrador() {
        return rol != null && rol.esAdministrador();
    }

    public boolean estaBloqueado(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    public boolean tieneBloqueoVencido(Instant ahora) {
        return bloqueadoHasta != null && !ahora.isBefore(bloqueadoHasta);
    }

    public long minutosDeBloqueoRestantes(Instant ahora) {
        if (!estaBloqueado(ahora)) return 0;
        long segundos = Duration.between(ahora, bloqueadoHasta).getSeconds();
        return Math.max(1, (segundos + 59) / 60);
    }

    public void levantarBloqueo() {
        bloqueadoHasta = null;
        intentosFallidos = 0;
    }

    public boolean registrarIngresoFallido(Instant ahora, int maximoDeIntentos, int minutosDeBloqueo) {
        intentosFallidos++;
        if (intentosFallidos < maximoDeIntentos) return false;
        bloqueadoHasta = ahora.plus(Duration.ofMinutes(minutosDeBloqueo));
        return true;
    }

    public void registrarIngresoExitoso(Instant ahora) {
        intentosFallidos = 0;
        bloqueadoHasta = null;
        ultimoLoginEn = ahora;
    }

    public void cambiarPassword(String nuevoHash, Instant ahora) {
        passwordHash = nuevoHash;
        passwordActualizadoEn = ahora;
    }

    public void restablecerPassword(String nuevoHash, Instant ahora) {
        cambiarPassword(nuevoHash, ahora);
        levantarBloqueo();
    }

    public boolean sesionAnteriorAlUltimoCambioDePassword(Instant sesionEmitidaEn) {
        return sesionEmitidaEn.plusSeconds(1).isBefore(passwordActualizadoEn);
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
    public Instant passwordActualizadoEn() { return passwordActualizadoEn; }
    public boolean aceptoTerminos() { return aceptoTerminos; }
    public Instant terminosAceptadosEn() { return terminosAceptadosEn; }
    public Instant creadoEn() { return creadoEn; }
    public int intentosFallidos() { return intentosFallidos; }
    public Instant bloqueadoHasta() { return bloqueadoHasta; }
    public Instant ultimoLoginEn() { return ultimoLoginEn; }
    public String estado() { return estado; }
    public Rol rol() { return rol; }
}
