package co.edu.udea.brujula.dominio.modelo.consulta;

import java.util.List;

public record ComponentesDelBanco(long total, List<Conteo> componentes) {

    public record Conteo(Long id, String nombre, long cantidad) {
    }
}
