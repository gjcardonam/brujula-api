package co.edu.udea.brujula.dominio.modelo.consulta;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;

public record SiguienteDelSimulacro(boolean finalizado, String motivo, Ejercicio ejercicio,
                                    EstadoDelSimulacro estado) {

    public static final String POR_TIEMPO = "TIEMPO";
    public static final String SIN_EJERCICIOS = "SIN_EJERCICIOS";
    public static final String YA_FINALIZADO = "FINALIZADO";

    public static SiguienteDelSimulacro finalizado(String motivo, EstadoDelSimulacro estado) {
        return new SiguienteDelSimulacro(true, motivo, null, estado);
    }

    public static SiguienteDelSimulacro con(Ejercicio ejercicio, EstadoDelSimulacro estado) {
        return new SiguienteDelSimulacro(false, null, ejercicio, estado);
    }
}
