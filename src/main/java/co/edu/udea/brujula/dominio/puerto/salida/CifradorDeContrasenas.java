package co.edu.udea.brujula.dominio.puerto.salida;

public interface CifradorDeContrasenas {

    String cifrar(String contrasenaPlana);

    boolean coincide(String contrasenaPlana, String hash);

    String resumen(String valor);
}
