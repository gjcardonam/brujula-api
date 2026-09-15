package co.edu.udea.brujula.aplicacion.perfil;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.ActualizarPerfil;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import co.edu.udea.brujula.dominio.servicio.PoliticaDeNombres;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActualizacionDelPerfil implements ActualizarPerfil {

    private final UsuarioRepositorio usuarios;

    public ActualizacionDelPerfil(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional
    public Usuario actualizar(Long idUsuario, String nombre, String apellido) {
        List<String> errores = PoliticaDeNombres.revisar(nombre, apellido);
        if (!errores.isEmpty()) throw new DatosInvalidos(errores);

        Usuario usuario = usuarios.porId(idUsuario).orElseThrow(() -> new NoEncontrado("La cuenta no existe."));
        usuario.actualizarDatosPersonales(nombre, apellido);
        return usuarios.guardar(usuario);
    }
}
