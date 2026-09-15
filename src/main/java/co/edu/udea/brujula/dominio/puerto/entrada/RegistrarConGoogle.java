package co.edu.udea.brujula.dominio.puerto.entrada;

public interface RegistrarConGoogle {

    record RegistroPendiente(String registroToken, String email, String nombre, String apellido, boolean simulado) {
    }

    RegistroPendiente iniciar(String credencial);
}
