package co.edu.udea.brujula.apoyo;

import co.edu.udea.brujula.dominio.modelo.*;

import java.time.Instant;
import java.util.List;

/** Objetos de prueba armados a mano, para no repetir constructores largos en cada test. */
public final class Datos {

    public static final Componente ALGEBRA = new Componente(1L, "Álgebra y cálculo", "Activo");
    public static final Componente GEOMETRIA = new Componente(2L, "Geometría", "Activo");
    public static final Competencia INTERPRETACION = new Competencia(1L, "Interpretación y representación", "Activo");
    public static final Competencia ARGUMENTACION = new Competencia(3L, "Argumentación", "Activo");
    public static final NivelDificultad BASICO = new NivelDificultad(1L, "Básico");
    public static final Rol ESTUDIANTE = new Rol(2L, Rol.ESTUDIANTE);
    public static final Rol ADMINISTRADOR = new Rol(1L, Rol.ADMINISTRADOR);

    private static long secuencia = 1;

    private Datos() {
    }

    public static Opcion opcionCorrecta(long id) {
        return new Opcion(id, "La correcta", null, true, "Bien hecho.", null, 1);
    }

    public static Opcion distractor(long id, TipoError tipo) {
        return new Opcion(id, "Un distractor", null, false, "Por aquí no es.", tipo, 2);
    }

    public static Ejercicio ejercicio(Long id, Componente componente, Competencia competencia, List<Opcion> opciones) {
        return new Ejercicio(id, id == null ? null : id.intValue(), "Enunciado " + id, null, BASICO,
                componente, competencia, Ejercicio.ACTIVO, Instant.parse("2026-09-01T10:00:00Z"),
                9L, "Carolina Gómez", opciones);
    }

    public static Ejercicio ejercicioActivo(Long id) {
        return ejercicio(id, ALGEBRA, INTERPRETACION, List.of(opcionCorrecta(id * 10), distractor(id * 10 + 1, null)));
    }

    public static Intento intento(Componente componente, Competencia competencia, boolean correcto) {
        Ejercicio ejercicio = ejercicio(secuencia++, componente, competencia, List.of());
        return new Intento(secuencia, Instant.now(), correcto, 3, null, 7L, ejercicio,
                opcionCorrecta(1L), null, null);
    }

    public static Usuario estudiante(Long id, String email, String hash) {
        Usuario usuario = Usuario.crear("Ana María", "Pérez Gómez", email, null, hash, ESTUDIANTE,
                Instant.parse("2026-08-01T10:00:00Z"));
        usuario.asignarId(id);
        return usuario;
    }
}
