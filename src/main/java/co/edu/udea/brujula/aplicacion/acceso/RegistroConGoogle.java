package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarConGoogle;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.VerificadorDeGoogle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class RegistroConGoogle implements RegistrarConGoogle {

    private final UsuarioRepositorio usuarios;
    private final VerificadorDeGoogle google;
    private final ProveedorDeTokens tokens;

    public RegistroConGoogle(UsuarioRepositorio usuarios, VerificadorDeGoogle google, ProveedorDeTokens tokens) {
        this.usuarios = usuarios;
        this.google = google;
        this.tokens = tokens;
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroPendiente iniciar(String credencial) {
        VerificadorDeGoogle.CuentaDeGoogle cuenta = google.verificar(credencial);
        if (usuarios.existeConEmail(cuenta.email())) {
            throw new Conflicto("CUENTA_EXISTENTE",
                    "Ya existe una cuenta asociada al correo " + cuenta.email() + ". Inicia sesión con tu contraseña.",
                    Map.of("email", cuenta.email()));
        }
        String registroToken = tokens.emitirRegistro(new ProveedorDeTokens.RegistroPendiente(
                cuenta.sub(), cuenta.email(), cuenta.nombre(), cuenta.apellido()));
        return new RegistroPendiente(registroToken, cuenta.email(), cuenta.nombre(), cuenta.apellido(),
                google.estaSimulado());
    }
}
