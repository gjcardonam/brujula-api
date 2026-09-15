package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import co.edu.udea.brujula.dominio.modelo.SesionRevocada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.RenovarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.SesionRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class RenovacionDeSesion implements RenovarSesion {

    private static final Duration VIGENCIA_SUPUESTA = Duration.ofHours(3);

    private final SesionRepositorio sesiones;
    private final UsuarioRepositorio usuarios;
    private final ProveedorDeTokens tokens;
    private final Reloj reloj;

    public RenovacionDeSesion(SesionRepositorio sesiones, UsuarioRepositorio usuarios, ProveedorDeTokens tokens,
                              Reloj reloj) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public SesionIniciada renovar(Long idUsuario, String jti, Instant expiraEn) {
        Usuario usuario = usuarios.porId(idUsuario)
                .orElseThrow(() -> new NoEncontrado("La cuenta no existe."));
        if (!sesiones.estaRevocada(jti)) {
            Instant ahora = reloj.ahora();
            Instant vence = expiraEn != null ? expiraEn : ahora.plus(VIGENCIA_SUPUESTA);
            sesiones.revocar(new SesionRevocada(jti, idUsuario, ahora, vence));
        }
        ProveedorDeTokens.Sesion nueva = tokens.emitirSesion(usuario);
        return new SesionIniciada(nueva.token(), nueva.expiraEn(), usuario);
    }
}
