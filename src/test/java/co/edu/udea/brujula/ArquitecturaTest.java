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

@DisplayName("Reglas de dependencia entre capas")
class ArquitecturaTest {

    private static final Path FUENTES = Path.of("src/main/java/co/edu/udea/brujula");

    @Test
    void elDominioNoDependeDeNingunFramework() throws IOException {
        List<String> prohibidos = List.of("org.springframework", "jakarta.persistence", "jakarta.servlet",
                "jakarta.validation", "com.fasterxml.jackson", "io.jsonwebtoken");

        List<String> incumplimientos = revisarImports(FUENTES.resolve("dominio"), prohibidos);

        assertTrue(incumplimientos.isEmpty(),
                "El dominio debe poder compilarse sin el framework:\n" + String.join("\n", incumplimientos));
    }

    @Test
    void elDominioNoConoceALasCapasDeAfuera() throws IOException {
        List<String> prohibidos = List.of("co.edu.udea.brujula.aplicacion", "co.edu.udea.brujula.infraestructura");

        List<String> incumplimientos = revisarImports(FUENTES.resolve("dominio"), prohibidos);

        assertTrue(incumplimientos.isEmpty(),
                "El dominio es el centro y no debe mirar hacia afuera:\n" + String.join("\n", incumplimientos));
    }

    @Test
    void laAplicacionNoDependeDeLaInfraestructura() throws IOException {
        List<String> prohibidos = List.of("co.edu.udea.brujula.infraestructura", "jakarta.persistence",
                "jakarta.servlet", "org.springframework.web");

        List<String> incumplimientos = revisarImports(FUENTES.resolve("aplicacion"), prohibidos);

        assertTrue(incumplimientos.isEmpty(),
                "Los casos de uso hablan con puertos, no con adaptadores:\n" + String.join("\n", incumplimientos));
    }

    @Test
    void ningunCasoDeUsoDependeDeOtroCasoDeUso() throws IOException {
        List<String> incumplimientos = revisarImports(FUENTES.resolve("aplicacion"),
                List.of("co.edu.udea.brujula.aplicacion"));

        assertTrue(incumplimientos.isEmpty(),
                "Lo que dos casos de uso comparten es una regla de dominio o un puerto:\n"
                        + String.join("\n", incumplimientos));
    }

    @Test
    void cadaCasoDeUsoImplementaUnSoloPuertoDeEntrada() throws IOException {
        List<String> incumplimientos = new ArrayList<>();
        for (Path archivo : archivosDe(FUENTES.resolve("aplicacion"))) {
            String contenido = Files.readString(archivo);
            int puertos = contenido.contains(" implements ")
                    ? contenido.split(" implements ")[1].split("\\{")[0].split(",").length
                    : 0;
            if (puertos != 1) {
                incumplimientos.add(archivo.getFileName() + " implementa " + puertos + " puertos de entrada");
            }
        }
        assertTrue(incumplimientos.isEmpty(),
                "Una clase por caso de uso y un puerto por clase:\n" + String.join("\n", incumplimientos));
    }

    @Test
    void soloElAdaptadorDeRelojPreguntaLaHoraDelSistema() throws IOException {
        List<String> incumplimientos = new ArrayList<>();
        for (Path archivo : archivosDe(FUENTES)) {
            if (archivo.toString().endsWith("RelojDelSistema.java")) continue;
            if (Files.readString(archivo).contains("Instant.now()")) {
                incumplimientos.add(FUENTES.relativize(archivo).toString());
            }
        }
        assertTrue(incumplimientos.isEmpty(),
                "La hora entra por el puerto Reloj:\n" + String.join("\n", incumplimientos));
    }

    @Test
    void elCodigoNoLlevaComentarios() throws IOException {
        List<String> incumplimientos = new ArrayList<>();
        for (Path archivo : archivosDe(FUENTES)) {
            for (String linea : Files.readAllLines(archivo)) {
                String limpia = linea.strip();
                if (limpia.startsWith("//") || limpia.startsWith("/*") || limpia.startsWith("*")) {
                    incumplimientos.add(FUENTES.relativize(archivo) + " → " + limpia);
                }
            }
        }
        assertTrue(incumplimientos.isEmpty(),
                "El nombre explica, el comentario sobra:\n" + String.join("\n", incumplimientos));
    }

    private List<Path> archivosDe(Path carpeta) throws IOException {
        try (Stream<Path> archivos = Files.walk(carpeta)) {
            return archivos.filter(ruta -> ruta.toString().endsWith(".java")).sorted().toList();
        }
    }

    private List<String> revisarImports(Path carpeta, List<String> importsProhibidos) throws IOException {
        List<String> incumplimientos = new ArrayList<>();
        for (Path archivo : archivosDe(carpeta)) {
            for (String linea : Files.readAllLines(archivo)) {
                if (!linea.startsWith("import ")) continue;
                for (String prohibido : importsProhibidos) {
                    if (linea.contains(prohibido)) {
                        incumplimientos.add(carpeta.relativize(archivo) + " → " + linea.trim());
                    }
                }
            }
        }
        return incumplimientos;
    }
}
