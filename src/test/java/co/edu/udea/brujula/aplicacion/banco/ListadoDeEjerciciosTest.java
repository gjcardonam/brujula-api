package co.edu.udea.brujula.aplicacion.banco;

import co.edu.udea.brujula.apoyo.Datos;
import co.edu.udea.brujula.apoyo.dobles.EjerciciosEnMemoria;
import co.edu.udea.brujula.apoyo.dobles.ParametrosEnMemoria;
import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.ParametrosDelSistema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Listado de ejercicios del banco")
class ListadoDeEjerciciosTest {

    private final EjerciciosEnMemoria ejercicios = new EjerciciosEnMemoria();
    private final ParametrosEnMemoria parametros = new ParametrosEnMemoria()
            .con(ParametrosDelSistema.TAMANO_PAGINA_BANCO, 20);
    private final ListadoDeEjercicios listado = new ListadoDeEjercicios(ejercicios, parametros);

    @Test
    void el_estudiante_solo_ve_los_ejercicios_activos() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(Datos.ejercicioDesactivado(2L));

        Pagina<TarjetaDeEjercicio> pagina = listado.listar(false, null, 0);

        assertEquals(1, pagina.totalElementos());
        assertEquals(Ejercicio.ACTIVO, pagina.contenido().get(0).estado());
    }

    @Test
    void el_administrador_ve_activos_y_desactivados_con_su_estado() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(Datos.ejercicioDesactivado(2L));

        Pagina<TarjetaDeEjercicio> pagina = listado.listar(true, null, 0);

        assertEquals(2, pagina.totalElementos());
        assertEquals(List.of("Activo", "Desactivado"),
                pagina.contenido().stream().map(TarjetaDeEjercicio::estado).toList());
    }

    @Test
    void la_tarjeta_lleva_numero_componente_competencia_y_nivel() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));

        TarjetaDeEjercicio tarjeta = listado.listar(false, null, 0).contenido().get(0);

        assertEquals(1, tarjeta.numero());
        assertEquals(Datos.ALGEBRA.nombre(), tarjeta.componente());
        assertEquals(Datos.INTERPRETACION.nombre(), tarjeta.competencia());
        assertEquals(Datos.BASICO.nombre(), tarjeta.nivel());
    }

    @Test
    void muestra_veinte_tarjetas_por_pagina() {
        for (long id = 1; id <= 25; id++) {
            ejercicios.agregar(Datos.ejercicioActivo(id));
        }

        Pagina<TarjetaDeEjercicio> primera = listado.listar(false, null, 0);
        Pagina<TarjetaDeEjercicio> segunda = listado.listar(false, null, 1);

        assertEquals(20, primera.contenido().size());
        assertEquals(5, segunda.contenido().size());
        assertEquals(2, primera.totalPaginas());
        assertEquals(25, primera.totalElementos());
    }

    @Test
    void filtra_por_componente() {
        ejercicios.agregar(Datos.ejercicioActivo(1L));
        ejercicios.agregar(new Ejercicio(2L, 2, "Otro enunciado", null, Datos.BASICO, Datos.GEOMETRIA,
                Datos.INTERPRETACION, Ejercicio.ACTIVO, Datos.AHORA, 9L, "Carolina Gómez", List.of()));

        Pagina<TarjetaDeEjercicio> pagina = listado.listar(false, Datos.GEOMETRIA.id(), 0);

        assertEquals(1, pagina.totalElementos());
        assertEquals(Datos.GEOMETRIA.nombre(), pagina.contenido().get(0).componente());
    }

    @Test
    void devuelve_una_pagina_vacia_cuando_el_banco_no_tiene_ejercicios() {
        Pagina<TarjetaDeEjercicio> pagina = listado.listar(false, null, 0);

        assertTrue(pagina.contenido().isEmpty());
        assertEquals(0, pagina.totalElementos());
    }
}
