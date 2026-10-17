package co.edu.udea.brujula.apoyo;

import co.edu.udea.brujula.dominio.modelo.Competencia;
import co.edu.udea.brujula.dominio.modelo.Componente;
import co.edu.udea.brujula.dominio.modelo.DuracionSimulacro;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Intento;
import co.edu.udea.brujula.dominio.modelo.NivelDificultad;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Rol;
import co.edu.udea.brujula.dominio.modelo.Usuario;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class Datos {

    public static final Instant AHORA = Instant.parse("2026-09-08T14:00:00Z");
    public static final Componente ALGEBRA = new Componente(1L, "Álgebra y cálculo", "Activo");
    public static final Componente GEOMETRIA = new Componente(2L, "Geometría", "Activo");
    public static final Competencia INTERPRETACION = new Competencia(1L, "Interpretación y representación", "Activo");
    public static final Competencia ARGUMENTACION = new Competencia(3L, "Argumentación", "Activo");
    public static final NivelDificultad BASICO = new NivelDificultad(1L, "Básico");
    public static final DuracionSimulacro TREINTA_MINUTOS = new DuracionSimulacro(1L, 30);
    public static final Rol ESTUDIANTE = new Rol(2L, Rol.ESTUDIANTE);
    public static final Rol ADMINISTRADOR = new Rol(1L, Rol.ADMINISTRADOR);

    private Datos() {
    }

    public static Opcion opcionCorrecta(long id) {
        return new Opcion(id, "La correcta", null, true, "Bien hecho.", 1);
    }

    public static Opcion distractor(long id) {
        return new Opcion(id, "Un distractor", null, false, "Por aquí no es.", 2);
    }

    public static Opcion sinRetroalimentacion(long id) {
        return new Opcion(id, "Otro distractor", null, false, null, 3);
    }

    public static Ejercicio ejercicio(Long id, String estado, List<Opcion> opciones) {
        return new Ejercicio(id, id.intValue(), "Enunciado " + id, null, BASICO, ALGEBRA, INTERPRETACION, estado,
                AHORA, 9L, "Carolina Gómez", opciones);
    }

    public static Ejercicio ejercicioActivo(Long id) {
        return ejercicio(id, Ejercicio.ACTIVO, List.of(opcionCorrecta(id * 10), distractor(id * 10 + 1),
                sinRetroalimentacion(id * 10 + 2)));
    }

    public static Ejercicio ejercicioDesactivado(Long id) {
        return ejercicio(id, "Desactivado", List.of(opcionCorrecta(id * 10), distractor(id * 10 + 1)));
    }

    public static Ejercicio ejercicioDe(Long id, Componente componente, Competencia competencia) {
        return new Ejercicio(id, id.intValue(), "Enunciado " + id, null, BASICO, componente, competencia,
                Ejercicio.ACTIVO, AHORA, 9L, "Carolina Gómez",
                List.of(opcionCorrecta(id * 10), distractor(id * 10 + 1)));
    }

    public static Intento intento(Componente componente, Competencia competencia, boolean correcto) {
        return intento(1L, null, componente, competencia, correcto, 3);
    }

    public static Intento intento(Long idEstudiante, Long idSimulacro, Componente componente,
                                  Competencia competencia, boolean correcto, int confianza) {
        Ejercicio ejercicio = ejercicioDe(1L, componente, competencia);
        Opcion elegida = ejercicio.opciones().get(correcto ? 0 : 1);
        return Intento.nuevo(idEstudiante, ejercicio, elegida, confianza, UUID.randomUUID(), idSimulacro, AHORA);
    }

    public static Usuario estudiante(Long id, String email, String hash) {
        Usuario usuario = Usuario.crear("Ana María", "Pérez Gómez", email, null, hash, ESTUDIANTE, AHORA);
        usuario.asignarId(id);
        return usuario;
    }

    public static Usuario administrador(Long id, String email, String hash) {
        Usuario usuario = Usuario.crear("Carolina", "Gómez", email, null, hash, ADMINISTRADOR, AHORA);
        usuario.asignarId(id);
        return usuario;
    }
}
