package co.edu.udea.brujula.dominio.puerto.salida;

public interface NotificadorDeCorreo {

    void enviarEnlaceDeRecuperacion(String correo, String token, int minutosDeVigencia);
}
