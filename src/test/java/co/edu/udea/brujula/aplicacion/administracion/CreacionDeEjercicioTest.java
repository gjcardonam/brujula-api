package co.edu.udea.brujula.aplicacion.administracion;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.CatalogosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.CifradorFalso;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ImagenesEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.RelojFijo;
import co.edu.udea.brujula.apoyo.dobles.UsuariosEnMemoria;
import co.edu.udea.brujula.dominio.excepcion.Conflicto;
import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Creación de un ejercicio")
class CreacionDeEjercicioTest {

    private static final Long ADMINISTRADOR = 1L;

    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria();
    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final ImagenesEnMemoria imagenes = new ImagenesEnMemoria();
    private final RelojFijo reloj = new RelojFijo();
    private final CreacionDeEjercicio creacion =
            new CreacionDeEjercicio(ejercicios, new CatalogosEnMemoria(), usuarios, imagenes, reloj);

    @BeforeEach
    void prepararElAdministrador() {
        usuarios.agregar(Datos.administrador(ADMINISTRADOR, "admin@brujula.local",
                new CifradorFalso().cifrar("Admin.2026")));
    }

    private DatosDeEjercicio datos(String enunciado, List<DatosDeEjercicio.DatosDeOpcion> opciones) {
        return new DatosDeEjercicio(enunciado, null, Datos.ALGEBRA.id(), Datos.INTERPRETACION.id(),
                Datos.BASICO.id(), opciones);
    }

    private List<DatosDeEjercicio.DatosDeOpcion> dosOpciones() {
        return List.of(new DatosDeEjercicio.DatosDeOpcion("La correcta", null, true, "Muy bien."),
                new DatosDeEjercicio.DatosDeOpcion("Un distractor", null, false, "Revisa el procedimiento."));
    }

    @Test
    void crea_el_ejercicio_activo_con_su_numero_y_su_creador() {
        Ejercicio creado = creacion.crear(ADMINISTRADOR, datos("¿Cuánto es 2 + 2?", dosOpciones()));

        assertEquals(Ejercicio.ACTIVO, creado.estado());
        assertNotNull(creado.numero());
        assertEquals("Carolina Gómez", creado.nombreCreador());
        assertEquals(2, creado.opciones().size());
        assertEquals(reloj.ahora(), creado.creadoEn());
    }

    @Test
    void numera_las_opciones_en_el_orden_recibido() {
        Ejercicio creado = creacion.crear(ADMINISTRADOR, datos("¿Cuánto es 2 + 2?", dosOpciones()));

        assertEquals(List.of(1, 2), creado.opciones().stream().map(o -> o.orden()).toList());
        assertTrue(creado.opcionCorrecta().isPresent());
    }

    @Test
    void exige_exactamente_una_opcion_correcta() {
        List<DatosDeEjercicio.DatosDeOpcion> dosCorrectas = List.of(
                new DatosDeEjercicio.DatosDeOpcion("Una", null, true, "Bien."),
                new DatosDeEjercicio.DatosDeOpcion("Otra", null, true, "Bien."));

        assertThrows(DatosInvalidos.class, () -> creacion.crear(ADMINISTRADOR, datos("Enunciado", dosCorrectas)));
    }

    @Test
    void rechaza_opciones_duplicadas() {
        List<DatosDeEjercicio.DatosDeOpcion> repetidas = List.of(
                new DatosDeEjercicio.DatosDeOpcion("Igual", null, true, "Bien."),
                new DatosDeEjercicio.DatosDeOpcion("igual", null, false, "Mal."));

        assertThrows(DatosInvalidos.class, () -> creacion.crear(ADMINISTRADOR, datos("Enunciado", repetidas)));
    }

    @Test
    void exige_retroalimentacion_en_cada_opcion() {
        List<DatosDeEjercicio.DatosDeOpcion> sinExplicacion = List.of(
                new DatosDeEjercicio.DatosDeOpcion("La correcta", null, true, "Bien."),
                new DatosDeEjercicio.DatosDeOpcion("Un distractor", null, false, "  "));

        assertThrows(DatosInvalidos.class, () -> creacion.crear(ADMINISTRADOR, datos("Enunciado", sinExplicacion)));
    }

    @Test
    void rechaza_un_enunciado_vacio_o_sin_opciones() {
        assertThrows(DatosInvalidos.class, () -> creacion.crear(ADMINISTRADOR, datos("  ", dosOpciones())));
        assertThrows(DatosInvalidos.class, () -> creacion.crear(ADMINISTRADOR, datos("Enunciado", List.of())));
    }

    @Test
    void impide_repetir_un_enunciado_que_ya_existe() {
        creacion.crear(ADMINISTRADOR, datos("¿Cuánto es 2 + 2?", dosOpciones()));

        Conflicto error = assertThrows(Conflicto.class,
                () -> creacion.crear(ADMINISTRADOR, datos(" ¿cuánto es 2 + 2? ", dosOpciones())));

        assertEquals("ENUNCIADO_DUPLICADO", error.codigo());
    }

    @Test
    void rechaza_un_componente_desactivado_o_inexistente() {
        DatosDeEjercicio conComponenteRetirado = new DatosDeEjercicio("Enunciado", null, 9L,
                Datos.INTERPRETACION.id(), Datos.BASICO.id(), dosOpciones());

        DatosInvalidos error = assertThrows(DatosInvalidos.class,
                () -> creacion.crear(ADMINISTRADOR, conComponenteRetirado));

        assertEquals("COMPONENTE_INVALIDO", error.codigo());
    }

    @Test
    void rechaza_una_imagen_que_no_esta_en_el_servidor() {
        DatosDeEjercicio conImagenFantasma = new DatosDeEjercicio("Enunciado", "/api/archivos/inexistente.png",
                Datos.ALGEBRA.id(), Datos.INTERPRETACION.id(), Datos.BASICO.id(), dosOpciones());

        assertThrows(DatosInvalidos.class, () -> creacion.crear(ADMINISTRADOR, conImagenFantasma));
    }

    @Test
    void acepta_una_imagen_ya_subida() {
        imagenes.precargar("grafico.png");
        DatosDeEjercicio conImagen = new DatosDeEjercicio("Enunciado con gráfico", "/api/archivos/grafico.png",
                Datos.ALGEBRA.id(), Datos.INTERPRETACION.id(), Datos.BASICO.id(), dosOpciones());

        Ejercicio creado = creacion.crear(ADMINISTRADOR, conImagen);

        assertEquals("grafico.png", creado.imagen());
    }
}
