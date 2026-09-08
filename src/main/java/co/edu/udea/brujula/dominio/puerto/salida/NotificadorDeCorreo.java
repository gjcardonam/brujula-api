package co.edu.udea.brujula.dominio.puerto.salida;

public interface NotificadorDeCorreo {

    /** El caso de uso entrega el token; armar la URL del front es cosa del adaptador. */
    void enviarEnlaceDeRecuperacion(String correo, String token, int minutosDeVigencia);
}
