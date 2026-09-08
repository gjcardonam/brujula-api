package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.CredencialesInvalidas;
import co.edu.udea.brujula.dominio.excepcion.CuentaBloqueada;
import co.edu.udea.brujula.dominio.excepcion.ErrorDeNegocio;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.SesionIniciada;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.AutenticarUsuario;
import co.edu.udea.brujula.dominio.puerto.salida.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AutenticacionDeUsuarios implements AutenticarUsuario {

    private final UsuarioRepositorio usuarios;
    private final CifradorDeContrasenas cifrador;
    private final ProveedorDeTokens tokens;
    private final ParametrosDelSistema parametros;
    private final Reloj reloj;

    public AutenticacionDeUsuarios(UsuarioRepositorio usuarios, CifradorDeContrasenas cifrador,
                                   ProveedorDeTokens tokens, ParametrosDelSistema parametros, Reloj reloj) {
        this.usuarios = usuarios;
        this.cifrador = cifrador;
        this.tokens = tokens;
        this.parametros = parametros;
        this.reloj = reloj;
    }

    /**
     * El bloqueo por intentos fallidos tiene que quedar guardado aunque la petición termine en
     * error, por eso no se hace rollback cuando sale una excepción de negocio.
     */
    @Override
    @Transactional(noRollbackFor = ErrorDeNegocio.class)
    public SesionIniciada autenticar(String email, String password) {
        List<String> faltantes = new ArrayList<>();
        if (email == null || email.isBlank()) faltantes.add("El correo electrónico es obligatorio.");
        if (password == null || password.isEmpty()) faltantes.add("La contraseña es obligatoria.");
        if (!faltantes.isEmpty()) throw new DatosInvalidos(faltantes);

        Usuario usuario = usuarios.porEmail(email.trim()).orElseThrow(CredencialesInvalidas::new);
        int maximoDeIntentos = parametros.entero(ParametrosDelSistema.MAX_INTENTOS_LOGIN, 5);
        int minutosDeBloqueo = parametros.entero(ParametrosDelSistema.MINUTOS_BLOQUEO_LOGIN, 10);
        Instant ahora = reloj.ahora();

        if (usuario.estaBloqueado(ahora, minutosDeBloqueo)) {
            long faltan = usuario.minutosDeBloqueoRestantes(ahora, minutosDeBloqueo);
            throw new CuentaBloqueada("Demasiados intentos fallidos. Intenta de nuevo en " + faltan
                    + " minuto" + (faltan == 1 ? "" : "s") + ".");
        }
        if (usuario.fechaBloqueo() != null) {
            usuario.levantarBloqueo();   // ya pasó el castigo, vuelve a empezar el contador
        }
        if (!usuario.estaActivo()) {
            throw new CredencialesInvalidas();
        }
        if (!cifrador.coincide(password, usuario.passwordHash())) {
            boolean quedaBloqueada = usuario.registrarIngresoFallido(ahora, maximoDeIntentos);
            usuarios.guardar(usuario);
            if (quedaBloqueada) {
                throw new CuentaBloqueada("Alcanzaste el máximo de " + maximoDeIntentos + " intentos fallidos. "
                        + "La autenticación queda bloqueada durante " + minutosDeBloqueo + " minutos.");
            }
            throw new CredencialesInvalidas();
        }
        usuario.registrarIngresoExitoso(ahora);
        usuarios.guardar(usuario);

        var sesion = tokens.emitirSesion(usuario);
        return new SesionIniciada(sesion.token(), sesion.expiraEn(), usuario);
    }
}
