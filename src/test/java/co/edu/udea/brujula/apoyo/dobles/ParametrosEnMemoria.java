package co.edu.udea.brujula.apoyo.dobles;

import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;

import java.util.HashMap;
import java.util.Map;

public class ParametrosEnMemoria implements ParametrosDelSistema {

    private final Map<String, Integer> valores = new HashMap<>();

    public ParametrosEnMemoria con(String clave, int valor) {
        valores.put(clave, valor);
        return this;
    }

    @Override
    public int entero(String clave, int valorPorDefecto) {
        return valores.getOrDefault(clave, valorPorDefecto);
    }
}
