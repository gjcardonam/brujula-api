package co.edu.udea.brujula.dominio.modelo.consulta;

import java.util.List;

/** Filtros del banco con la cantidad de ejercicios que puede ver el usuario según su rol (HU-007 CA-07). */
public record ComponentesDelBanco(long total, List<Conteo> componentes) {

    public record Conteo(Long id, String nombre, long cantidad) {
    }
}
