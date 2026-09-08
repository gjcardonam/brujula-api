package co.edu.udea.brujula.dominio.modelo.consulta;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;

/** Si el simulacro terminó, `motivo` dice por qué: se acabó el tiempo o ya no quedan ejercicios. */
public record SiguienteDelSimulacro(boolean finalizado, String motivo, Ejercicio ejercicio, EstadoDelSimulacro estado) {

    public static final String POR_TIEMPO = "TIEMPO";
    public static final String SIN_EJERCICIOS = "SIN_EJERCICIOS";
    public static final String YA_FINALIZADO = "FINALIZADO";
}
