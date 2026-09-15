package co.edu.udea.brujula.aplicacion.perfil;

import co.edu.udea.brujula.dominio.excepcion.NoEncontrado;
import co.edu.udea.brujula.dominio.modelo.Usuario;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarPerfil;
import co.edu.udea.brujula.dominio.puerto.salida.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaDePerfil implements ConsultarPerfil {

    private final UsuarioRepositorio usuarios;

    public ConsultaDePerfil(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario consultar(Long idUsuario) {
        return usuarios.porId(idUsuario).orElseThrow(() -> new NoEncontrado("La cuenta no existe."));
    }
}
