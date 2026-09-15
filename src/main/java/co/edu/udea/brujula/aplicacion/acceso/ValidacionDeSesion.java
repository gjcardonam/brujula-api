package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import co.edu.udea.brujula.dominio.puerto.salida.SesionRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ValidacionDeSesion implements ValidarSesion {

    private final SesionRepositorio sesiones;
    private final UsuarioRepositorio usuarios;
    private final ProveedorDeTokens tokens;

    public ValidacionDeSesion(SesionRepositorio sesiones, UsuarioRepositorio usuarios, ProveedorDeTokens tokens) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.tokens = tokens;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Autenticado> validar(String token) {
        Optional<ProveedorDeTokens.SesionLeida> leida = tokens.leerSesion(token);
        if (leida.isEmpty()) return Optional.empty();

        ProveedorDeTokens.SesionLeida sesion = leida.get();
        if (sesiones.estaRevocada(sesion.jti())) return Optional.empty();

        return usuarios.porId(sesion.idUsuario())
                .filter(Usuario::estaActivo)
                .filter(usuario -> !usuario.sesionAnteriorAlUltimoCambioDePassword(sesion.emitidoEn()))
                .map(usuario -> new Autenticado(usuario.id(), usuario.email(), usuario.rol().nombre(),
                        sesion.jti(), sesion.expiraEn()));
    }
}
