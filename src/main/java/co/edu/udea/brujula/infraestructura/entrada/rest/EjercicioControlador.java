package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.*;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.CambioEstadoRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.OpcionRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.EjercicioRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.EjercicioAdminDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.EjercicioEstudianteDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.PaginaDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioControlador {

    private final ConsultarBanco banco;
    private final ConsultarEjercicio consultaDeEjercicio;
    private final BuscarSiguienteEjercicio siguiente;
    private final AdministrarEjercicios administracion;
    private final AlmacenDeImagenes imagenes;

    public EjercicioControlador(ConsultarBanco banco, ConsultarEjercicio consultaDeEjercicio,
                                BuscarSiguienteEjercicio siguiente, AdministrarEjercicios administracion,
                                AlmacenDeImagenes imagenes) {
        this.banco = banco;
        this.consultaDeEjercicio = consultaDeEjercicio;
        this.siguiente = siguiente;
        this.administracion = administracion;
        this.imagenes = imagenes;
    }

    @GetMapping
    public PaginaDto<TarjetaDeEjercicio> listar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                                @RequestParam(required = false) Long componente,
                                                @RequestParam(defaultValue = "0") int pagina) {
        return PaginaDto.de(banco.listar(usuario.esAdministrador(), componente, pagina));
    }

    @GetMapping("/componentes")
    public ComponentesDelBanco componentes(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return banco.componentes(usuario.esAdministrador());
    }

    /** El administrador recibe el detalle completo; el estudiante, la versión sin respuestas. */
    @GetMapping("/{id}")
    public Object consultar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario, @PathVariable Long id) {
        if (usuario.esAdministrador()) {
            return EjercicioAdminDto.de(administracion.consultarDetalle(id), imagenes);
        }
        var paraPracticar = consultaDeEjercicio.paraPracticar(usuario.id(), id);
        return EjercicioEstudianteDto.de(paraPracticar.ejercicio(), paraPracticar.intentosPrevios(), imagenes);
    }

    @GetMapping("/{id}/siguiente")
    public Map<String, Object> siguiente(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                         @PathVariable Long id,
                                         @RequestParam(required = false) Long componente) {
        var resultado = siguiente.buscar(usuario.id(), id, componente);
        return resultado.hayMas()
                ? Map.of("hayMas", true, "idEjercicio", resultado.idEjercicio())
                : Map.of("hayMas", false, "mensaje", resultado.mensaje());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public EjercicioAdminDto crear(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                   @RequestBody EjercicioRequest peticion) {
        return EjercicioAdminDto.de(administracion.crear(usuario.id(), aComando(peticion)), imagenes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EjercicioAdminDto editar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                    @PathVariable Long id, @RequestBody EjercicioRequest peticion) {
        return EjercicioAdminDto.de(administracion.editar(usuario.id(), id, aComando(peticion)), imagenes);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EjercicioAdminDto cambiarEstado(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                           @PathVariable Long id, @RequestBody CambioEstadoRequest peticion) {
        if (peticion == null || peticion.estado() == null) {
            throw new DatosInvalidos("ESTADO_INVALIDO", "Debes indicar el estado.");
        }
        return EjercicioAdminDto.de(administracion.cambiarEstado(usuario.id(), id, peticion.estado()), imagenes);
    }

    private DatosDeEjercicio aComando(EjercicioRequest peticion) {
        List<DatosDeEjercicio.DatosDeOpcion> opciones = (peticion.opciones() == null ? List.<OpcionRequest>of() : peticion.opciones())
                .stream()
                .map(o -> new DatosDeEjercicio.DatosDeOpcion(o.id(), o.descripcion(), o.imagen(),
                        Boolean.TRUE.equals(o.esCorrecta()), o.retroalimentacion(), o.idTipoError()))
                .toList();
        return new DatosDeEjercicio(peticion.enunciado(), peticion.imagenEnunciado(), peticion.idComponente(),
                peticion.idCompetencia(), peticion.idNivelDificultad(), opciones);
    }
}
