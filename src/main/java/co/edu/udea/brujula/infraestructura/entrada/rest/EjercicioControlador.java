package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.excepcion.DatosInvalidos;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.modelo.consulta.DetalleDeEjercicio;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;
import co.edu.udea.brujula.dominio.modelo.consulta.UsoDelEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.AbrirEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.BuscarSiguienteEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.CambiarEstadoDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarDetalleDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.entrada.CrearEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.EditarEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ListarEjercicios;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.dominio.servicio.Porcentajes;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.CambioEstadoRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.EjercicioRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.OpcionRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.EjercicioAdminDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.EjercicioEstudianteDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.PaginaDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioControlador {

    private final ListarEjercicios listado;
    private final ConsultarComponentesDelBanco componentesDelBanco;
    private final AbrirEjercicio apertura;
    private final BuscarSiguienteEjercicio siguiente;
    private final CrearEjercicio creacion;
    private final EditarEjercicio edicion;
    private final CambiarEstadoDeEjercicio cambioDeEstado;
    private final ConsultarDetalleDeEjercicio consultaDeDetalle;
    private final AlmacenDeImagenes imagenes;

    public EjercicioControlador(ListarEjercicios listado, ConsultarComponentesDelBanco componentesDelBanco,
                                AbrirEjercicio apertura, BuscarSiguienteEjercicio siguiente, CrearEjercicio creacion,
                                EditarEjercicio edicion, CambiarEstadoDeEjercicio cambioDeEstado,
                                ConsultarDetalleDeEjercicio consultaDeDetalle, AlmacenDeImagenes imagenes) {
        this.listado = listado;
        this.componentesDelBanco = componentesDelBanco;
        this.apertura = apertura;
        this.siguiente = siguiente;
        this.creacion = creacion;
        this.edicion = edicion;
        this.cambioDeEstado = cambioDeEstado;
        this.consultaDeDetalle = consultaDeDetalle;
        this.imagenes = imagenes;
    }

    @GetMapping
    public PaginaDto<TarjetaDeEjercicio> listar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                                @RequestParam(required = false) Long componente,
                                                @RequestParam(defaultValue = "0") int pagina) {
        return PaginaDto.de(listado.listar(usuario.esAdministrador(), componente, pagina));
    }

    @GetMapping("/componentes")
    public ComponentesDelBanco componentes(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return componentesDelBanco.componentes(usuario.esAdministrador());
    }

    @GetMapping("/{id}")
    public Object consultar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario, @PathVariable Long id) {
        if (usuario.esAdministrador()) {
            return EjercicioAdminDto.de(consultaDeDetalle.detalle(id), imagenes);
        }
        AbrirEjercicio.ParaPracticar abierto = apertura.abrir(usuario.id(), id);
        return EjercicioEstudianteDto.de(abierto.ejercicio(), abierto.intentosPrevios(), imagenes);
    }

    @GetMapping("/{id}/siguiente")
    public Map<String, Object> siguiente(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                         @PathVariable Long id,
                                         @RequestParam(required = false) Long componente) {
        BuscarSiguienteEjercicio.Siguiente resultado = siguiente.buscar(usuario.id(), id, componente);
        return resultado.hayMas()
                ? Map.of("hayMas", true, "idEjercicio", resultado.idEjercicio())
                : Map.of("hayMas", false, "mensaje", resultado.mensaje());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public EjercicioAdminDto crear(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                   @RequestBody EjercicioRequest peticion) {
        var ejercicio = creacion.crear(usuario.id(), aComando(peticion));
        var sinUso = new DetalleDeEjercicio(ejercicio, new UsoDelEjercicio(0, 0, Porcentajes.cero(), List.of()),
                Set.of());
        return EjercicioAdminDto.de(sinUso, imagenes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EjercicioAdminDto editar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                    @PathVariable Long id, @RequestBody EjercicioRequest peticion) {
        return EjercicioAdminDto.de(edicion.editar(usuario.id(), id, aComando(peticion)), imagenes);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public EjercicioAdminDto cambiarEstado(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                           @PathVariable Long id, @RequestBody CambioEstadoRequest peticion) {
        if (peticion == null || peticion.estado() == null) {
            throw new DatosInvalidos("ESTADO_INVALIDO", "Debes indicar el estado.");
        }
        return EjercicioAdminDto.de(cambioDeEstado.cambiarEstado(usuario.id(), id, peticion.estado()), imagenes);
    }

    private static DatosDeEjercicio aComando(EjercicioRequest peticion) {
        List<OpcionRequest> recibidas = peticion.opciones() == null ? List.of() : peticion.opciones();
        List<DatosDeEjercicio.DatosDeOpcion> opciones = recibidas.stream()
                .map(opcion -> new DatosDeEjercicio.DatosDeOpcion(opcion.id(), opcion.descripcion(),
                        opcion.imagen(), Boolean.TRUE.equals(opcion.esCorrecta()), opcion.retroalimentacion(),
                        opcion.idTipoError()))
                .toList();
        return new DatosDeEjercicio(peticion.enunciado(), peticion.imagenEnunciado(), peticion.idComponente(),
                peticion.idCompetencia(), peticion.idNivelDificultad(), opciones);
    }
}
