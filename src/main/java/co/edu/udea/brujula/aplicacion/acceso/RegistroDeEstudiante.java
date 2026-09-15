package co.edu.udea.brujula.aplicacion.acceso;

import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.RegistrarEstudiante;
import co.edu.udea.brujula.dominio.puerto.salida.CatalogoRepositorio;
import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;
import co.edu.udea.brujula.dominio.puerto.salida.ProveedorDeTokens;
import co.edu.udea.brujula.dominio.puerto.salida.Reloj;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeContrasena;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeNombres;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegistroDeEstudiante implements RegistrarEstudiante {

    private final UsuarioRepositorio usuarios;
    private final CatalogoRepositorio catalogos;
    private final ProveedorDeTokens tokens;
    private final CifradorDeContrasenas cifrador;
    private final Reloj reloj;

    public RegistroDeEstudiante(UsuarioRepositorio usuarios, CatalogoRepositorio catalogos, ProveedorDeTokens tokens,
                                CifradorDeContrasenas cifrador, Reloj reloj) {
        this.usuarios = usuarios;
        this.catalogos = catalogos;
        this.tokens = tokens;
        this.cifrador = cifrador;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Usuario registrar(Datos datos) {
        ProveedorDeTokens.RegistroPendiente verificado = tokens.leerRegistro(datos.registroToken())
                .orElseThrow(() -> new DatosInvalidos("REGISTRO_EXPIRADO",
                        "La verificación con Google expiró. Vuelve a seleccionar \"Continuar con Google\"."));
        revisar(datos);
        if (usuarios.existeConEmail(verificado.email())) {
            throw new Conflicto("CUENTA_EXISTENTE",
                    "Ya existe una cuenta asociada al correo " + verificado.email() + ".");
        }
        Rol estudiante = catalogos.rol(Rol.ESTUDIANTE)
                .orElseThrow(() -> new IllegalStateException("Falta el rol Estudiante en la base de datos"));
        return usuarios.guardar(Usuario.crear(datos.nombre(), datos.apellido(), verificado.email(),
                verificado.googleSub(), cifrador.cifrar(datos.password()), estudiante, reloj.ahora()));
    }

    private static void revisar(Datos datos) {
        List<String> errores = new ArrayList<>(PoliticaDeNombres.revisar(datos.nombre(), datos.apellido()));
        errores.addAll(PoliticaDeContrasena.revisar(datos.password(), datos.confirmacionPassword()));
        if (!Boolean.TRUE.equals(datos.aceptoTerminos())) {
            errores.add("Debes aceptar los Términos y Condiciones y la Política de Tratamiento de Datos Personales.");
        }
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);
    }
}
