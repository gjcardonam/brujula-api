package co.edu.udea.brujula.aplicacion;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarEstudiante;
import co.edu.udea.brujula.dominio.puerto.salida.*;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeContrasena;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RegistroDeEstudiantes implements RegistrarEstudiante {

    private final UsuarioRepositorio usuarios;
    private final CatalogoRepositorio catalogos;
    private final VerificadorDeGoogle google;
    private final ProveedorDeTokens tokens;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;

    public RegistroDeEstudiantes(UsuarioRepositorio usuarios, CatalogoRepositorio catalogos, VerificadorDeGoogle google,
                                 ProveedorDeTokens tokens, CifradorDeContrasenas cifrador, Reloj reloj) {
        this.usuarios = usuarios;
        this.catalogos = catalogos;
        this.google = google;
        this.tokens = tokens;
        this.cifrador = cifrador;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public RegistroPendiente iniciarConGoogle(String credencial) {
        var cuenta = google.verificar(credencial);
        if (usuarios.existeConEmail(cuenta.email())) {
            // No se crea una segunda cuenta; el front lleva al inicio de sesión (CA-03).
            throw new Conflicto("CUENTA_EXISTENTE",
                    "Ya existe una cuenta asociada al correo " + cuenta.email() + ". Inicia sesión con tu contraseña.",
                    Map.of("email", cuenta.email()));
        }
        String token = tokens.emitirRegistro(new ProveedorDeTokens.RegistroPendiente(
                cuenta.sub(), cuenta.email(), cuenta.nombre(), cuenta.apellido()));
        return new RegistroPendiente(token, cuenta.email(), cuenta.nombre(), cuenta.apellido(), google.estaSimulado());
    }

    @Override
    @Transactional
    public Usuario registrar(Datos datos) {
        var verificado = tokens.leerRegistro(datos.registroToken())
                .orElseThrow(() -> new DatosInvalidos("REGISTRO_EXPIRADO",
                        "La verificación con Google expiró. Vuelve a seleccionar \"Continuar con Google\"."));

        List<String> errores = new ArrayList<>(PoliticaDeContrasena.revisarNombres(datos.nombre(), datos.apellido()));
        errores.addAll(PoliticaDeContrasena.revisar(datos.password(), datos.confirmacionPassword()));
        if (!Boolean.TRUE.equals(datos.aceptoTerminos())) {
            errores.add("Debes aceptar los Términos y Condiciones y la Política de Tratamiento de Datos Personales.");
        }
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        if (usuarios.existeConEmail(verificado.email())) {
            throw new Conflicto("CUENTA_EXISTENTE", "Ya existe una cuenta asociada al correo " + verificado.email() + ".");
        }
        Rol estudiante = catalogos.rol(Rol.ESTUDIANTE)
                .orElseThrow(() -> new IllegalStateException("Falta el rol Estudiante en la base de datos"));
        Usuario nuevo = Usuario.crear(datos.nombre(), datos.apellido(), verificado.email(),
                verificado.googleSub(), cifrador.cifrar(datos.password()), estudiante, reloj.ahora());
        return usuarios.guardar(nuevo);
    }
}
