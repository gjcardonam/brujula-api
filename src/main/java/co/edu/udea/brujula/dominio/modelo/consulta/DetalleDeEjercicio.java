package co.edu.udea.brujula.dominio.modelo.consulta;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;

import java.util.Set;

public record DetalleDeEjercicio(Ejercicio ejercicio, UsoDelEjercicio uso, Set<Long> opcionesUsadas) {

    public boolean tieneIntentos() {
        return uso.intentos() > 0;
    }
}
