package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.SesionIniciada;

/** HU-002. */
public interface AutenticarUsuario {
    SesionIniciada autenticar(String email, String password);
}
