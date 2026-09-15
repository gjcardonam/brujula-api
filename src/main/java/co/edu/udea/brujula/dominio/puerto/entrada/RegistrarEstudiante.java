package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Usuario;

public interface RegistrarEstudiante {

    record Datos(String registroToken, String nombre, String apellido, String password, String confirmacionPassword,
                 Boolean aceptoTerminos) {
    }

    Usuario registrar(Datos datos);
}
