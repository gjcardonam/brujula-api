package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.puerto.salida.NotificadorDeCorreo;

import java.util.ArrayList;
import java.util.List;

public class CorreoFalso implements NotificadorDeCorreo {

    public record Enviado(String correo, String token, int minutosDeVigencia) {
    }

    private final List<Enviado> enviados = new ArrayList<>();

    public List<Enviado> enviados() {
        return enviados;
    }

    @Override
    public void enviarEnlaceDeRecuperacion(String correo, String token, int minutosDeVigencia) {
        enviados.add(new Enviado(correo, token, minutosDeVigencia));
    }
}
