package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.excepcion.CredencialesInvalidas;
import co.edu.udea.brujula.dominio.excepcion.CuentaBloqueada;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.ErrorDeNegocio;
import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.IniciarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class InicioDeSesion implements IniciarSesion {

    private final UsuarioRepositorio usuarios;
    private final CifradorDeContrasenas cifrador;
    private final ProveedorDeTokens tokens;
    private final ParametrosDelSistema parametros;
    private final Reloj reloj;

    public InicioDeSesion(UsuarioRepositorio usuarios, CifradorDeContrasenas cifrador, ProveedorDeTokens tokens,
                          ParametrosDelSistema parametros, Reloj reloj) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.tokens = tokens;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    @Override
    @Transactional(noRollbackFor = ErrorDeNegocio.class)
    public SesionIniciada iniciar(String email, String password) {
        exigirCredenciales(email, password);
        Usuario usuario = usuarios.porEmail(email.trim()).orElseThrow(CredencialesInvalidas::new);
        Instant ahora = reloj.ahora();

        if (usuario.estaBloqueado(ahora)) {
            long faltan = usuario.minutosDeBloqueoRestantes(ahora);
            throw new CuentaBloqueada("Demasiados intentos fallidos. Intenta de nuevo en " + faltan
                    + " minuto" + (faltan == 1 ? "" : "s") + ".");
        }
        if (usuario.tieneBloqueoVencido(ahora)) usuario.levantarBloqueo();
        if (!usuario.estaActivo()) throw new CredencialesInvalidas();
        if (!cifrador.coincide(password, usuario.passwordHash())) {
            throw rechazar(usuario, ahora);
        }
        usuario.registrarIngresoExitoso(ahora);
        usuarios.guardar(usuario);

        ProveedorDeTokens.Sesion sesion = tokens.emitirSesion(usuario);
        return new SesionIniciada(sesion.token(), sesion.expiraEn(), usuario);
    }

    private ErrorDeNegocio rechazar(Usuario usuario, Instant ahora) {
        int maximoDeIntentos = parametros.entero(ParametrosDelSistema.MAX_INTENTOS_LOGIN, 5);
        int minutosDeBloqueo = parametros.entero(ParametrosDelSistema.MINUTOS_BLOQUEO_LOGIN, 10);
        boolean quedaBloqueada = usuario.registrarIngresoFallido(ahora, maximoDeIntentos, minutosDeBloqueo);
        usuarios.guardar(usuario);
        if (!quedaBloqueada) return new CredencialesInvalidas();
        return new CuentaBloqueada("Alcanzaste el máximo de " + maximoDeIntentos + " intentos fallidos. "
                + "La autenticación queda bloqueada durante " + minutosDeBloqueo + " minutos.");
    }

    private static void exigirCredenciales(String email, String password) {
        List<String> faltantes = new ArrayList<>();
        if (email == null || email.isBlank()) faltantes.add("El correo electrónico es obligatorio.");
        if (password == null || password.isEmpty()) faltantes.add("La contraseña es obligatoria.");
        if (!faltantes.isEmpty()) throw new DatosInvalidos(faltantes);
    }
}
