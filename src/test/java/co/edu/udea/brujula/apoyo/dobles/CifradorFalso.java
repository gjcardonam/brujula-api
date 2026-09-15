package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.puerto.salida.CifradorDeContrasenas;

public class CifradorFalso implements CifradorDeContrasenas {

    @Override
    public String cifrar(String contrasenaPlana) {
        return "hash:" + contrasenaPlana;
    }

    @Override
    public boolean coincide(String contrasenaPlana, String hash) {
        return cifrar(contrasenaPlana).equals(hash);
    }

    @Override
    public String resumen(String valor) {
        return "sha:" + valor;
    }
}
