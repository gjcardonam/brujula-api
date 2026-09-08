package co.edu.udea.brujula.dominio.puerto.entrada;

import co.edu.udea.brujula.dominio.modelo.Usuario;

/** HU-001: registro en dos pasos, primero Google verifica el correo y después se completan los datos. */
public interface RegistrarEstudiante {

    record RegistroPendiente(String registroToken, String email, String nombre, String apellido, boolean simulado) {
    }

    record Datos(String registroToken, String nombre, String apellido, String password,
                 String confirmacionPassword, Boolean aceptoTerminos) {
    }

    RegistroPendiente iniciarConGoogle(String credencial);

    Usuario registrar(Datos datos);
}
