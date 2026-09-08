package co.edu.udea.brujula.dominio.modelo.consulta;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;

import java.util.Set;

/** Vista de administrador: el ejercicio, su uso y qué opciones ya no se pueden tocar (HU-021 CA-09). */
public record DetalleDeEjercicio(Ejercicio ejercicio, UsoDelEjercicio uso, Set<Long> opcionesUsadas) {

    public boolean tieneIntentos() {
        return uso.intentos() > 0;
    }
}
