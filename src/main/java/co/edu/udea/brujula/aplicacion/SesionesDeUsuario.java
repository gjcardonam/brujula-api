package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import co.edu.udea.brujula.dominio.modelo.SesionRevocada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.GestionarSesion;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SesionRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class SesionesDeUsuario implements GestionarSesion, ValidarSesion {

    private final SesionRepositorio sesiones;
    private final UsuarioRepositorio usuarios;
    private final ProveedorDeTokens tokens;
    private final Reloj reloj;

    public SesionesDeUsuario(SesionRepositorio sesiones, UsuarioRepositorio usuarios,
                             ProveedorDeTokens tokens, Reloj reloj) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Autenticado> validar(String token) {
        var leida = tokens.leerSesion(token);
        if (leida.isEmpty()) return Optional.empty();

        var sesion = leida.get();
        if (sesiones.estaRevocada(sesion.jti())) return Optional.empty();

        return usuarios.porId(sesion.idUsuario())
                .filter(Usuario::estaActivo)
                .filter(u -> emitidaDespuesDelUltimoCambio(sesion, u))
                .map(u -> new Autenticado(u.id(), u.email(), u.rol().nombre(), sesion.jti(), sesion.expiraEn()));
    }

    /**
     * Al restablecer la contraseña se invalidan las sesiones que ya estaban abiertas (HU-003 CA-07).
     * El token guarda los segundos, así que se tolera el mismo segundo del cambio.
     */
    private boolean emitidaDespuesDelUltimoCambio(ProveedorDeTokens.SesionLeida sesion, Usuario usuario) {
        if (usuario.passwordActualizadoEn() == null) return true;
        return !sesion.emitidoEn().plusSeconds(1).isBefore(usuario.passwordActualizadoEn());
    }

    @Override
    @Transactional
    public void cerrar(Long idUsuario, String jti, Instant expiraEn) {
        revocar(idUsuario, jti, expiraEn);
    }

    @Override
    @Transactional
    public SesionIniciada renovar(Long idUsuario, String jti, Instant expiraEn) {
        Usuario usuario = usuarios.porId(idUsuario).orElseThrow();
        revocar(idUsuario, jti, expiraEn);
        var nueva = tokens.emitirSesion(usuario);
        return new SesionIniciada(nueva.token(), nueva.expiraEn(), usuario);
    }

    private void revocar(Long idUsuario, String jti, Instant expiraEn) {
        if (sesiones.estaRevocada(jti)) return;
        Instant vence = expiraEn != null ? expiraEn : reloj.ahora().plus(Duration.ofHours(3));
        sesiones.revocar(new SesionRevocada(jti, idUsuario, reloj.ahora(), vence));
    }

    @Override
    @Transactional
    public int limpiarRevocadasVencidas() {
        return sesiones.borrarExpiradas(reloj.ahora());
    }
}
