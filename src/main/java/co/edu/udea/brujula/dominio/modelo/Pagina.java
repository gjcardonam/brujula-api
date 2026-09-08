package co.edu.udea.brujula.dominio.modelo;

import java.util.List;
import java.util.function.Function;

public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <T> Pagina<T> de(List<T> contenido, int pagina, int tamano, long totalElementos) {
        int paginas = tamano <= 0 ? 0 : (int) Math.ceil((double) totalElementos / tamano);
        return new Pagina<>(contenido, pagina, tamano, totalElementos, paginas);
    }

    public <R> Pagina<R> mapear(Function<T, R> f) {
        return new Pagina<>(contenido.stream().map(f).toList(), pagina, tamano, totalElementos, totalPaginas);
    }

    public static <T> Pagina<T> vacia(int tamano) {
        return new Pagina<>(List.of(), 0, tamano, 0, 0);
    }
}
