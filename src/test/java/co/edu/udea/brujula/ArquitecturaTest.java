package co.edu.udea.brujula;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La arquitectura hexagonal solo sirve si las dependencias apuntan hacia adentro. Esta prueba lo
 * verifica leyendo los imports, para que la regla no se rompa sin que nadie se dé cuenta.
 */
@DisplayName("Reglas de dependencia entre capas")
class ArquitecturaTest {

    private static final Path FUENTES = Path.of("src/main/java/co/edu/udea/brujula");

    @Test
    void elDominioNoDependeDeNingunFramework() throws IOException {
        List<String> prohibidos = List.of("org.springframework", "jakarta.persistence", "jakarta.servlet",
                "jakarta.validation", "com.fasterxml.jackson", "io.jsonwebtoken");

        List<String> incumplimientos = revisar(FUENTES.resolve("dominio"), prohibidos);

        assertTrue(incumplimientos.isEmpty(),
                "El dominio debe poder compilarse sin el framework:\n" + String.join("\n", incumplimientos));
    }

    @Test
    void elDominioNoConoceALasCapasDeAfuera() throws IOException {
        List<String> prohibidos = List.of("co.edu.udea.brujula.aplicacion", "co.edu.udea.brujula.infraestructura");

        List<String> incumplimientos = revisar(FUENTES.resolve("dominio"), prohibidos);

        assertTrue(incumplimientos.isEmpty(),
                "El dominio es el centro y no debe mirar hacia afuera:\n" + String.join("\n", incumplimientos));
    }

    @Test
    void laAplicacionNoDependeDeLaInfraestructura() throws IOException {
        List<String> prohibidos = List.of("co.edu.udea.brujula.infraestructura", "jakarta.persistence",
                "jakarta.servlet", "org.springframework.web");

        List<String> incumplimientos = revisar(FUENTES.resolve("aplicacion"), prohibidos);

        assertTrue(incumplimientos.isEmpty(),
                "Los casos de uso hablan con puertos, no con adaptadores:\n" + String.join("\n", incumplimientos));
    }

    private List<String> revisar(Path carpeta, List<String> importsProhibidos) throws IOException {
        List<String> incumplimientos = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(carpeta)) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                for (String linea : Files.readAllLines(archivo)) {
                    if (!linea.startsWith("import ")) continue;
                    for (String prohibido : importsProhibidos) {
                        if (linea.contains(prohibido)) {
                            incumplimientos.add(carpeta.relativize(archivo) + " → " + linea.trim());
                        }
                    }
                }
            }
        }
        return incumplimientos;
    }
}
