package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.modelo.consulta.TarjetaDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.AbrirEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.BuscarSiguienteEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.entrada.CrearEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ListarEjercicios;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.entrada.comando.DatosDeEjercicio;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.EjercicioRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.OpcionRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.EjercicioAdminDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.EjercicioEstudianteDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.PaginaDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ejercicios")
public class EjercicioControlador {

    private final ListarEjercicios listado;
    private final ConsultarComponentesDelBanco componentesDelBanco;
    private final AbrirEjercicio apertura;
    private final BuscarSiguienteEjercicio siguiente;
    private final CrearEjercicio creacion;
    private final AlmacenDeImagenes imagenes;

    public EjercicioControlador(ListarEjercicios listado, ConsultarComponentesDelBanco componentesDelBanco,
                                AbrirEjercicio apertura, BuscarSiguienteEjercicio siguiente,
                                CrearEjercicio creacion, AlmacenDeImagenes imagenes) {
        this.listado = listado;
        this.componentesDelBanco = componentesDelBanco;
        this.apertura = apertura;
        this.siguiente = siguiente;
        this.creacion = creacion;
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
    public EjercicioEstudianteDto consultar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                            @PathVariable Long id) {
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
        return EjercicioAdminDto.de(creacion.crear(usuario.id(), aComando(peticion)), imagenes);
    }

    private static DatosDeEjercicio aComando(EjercicioRequest peticion) {
        List<OpcionRequest> recibidas = peticion.opciones() == null ? List.of() : peticion.opciones();
        List<DatosDeEjercicio.DatosDeOpcion> opciones = recibidas.stream()
                .map(opcion -> new DatosDeEjercicio.DatosDeOpcion(opcion.descripcion(), opcion.imagen(),
                        Boolean.TRUE.equals(opcion.esCorrecta()), opcion.retroalimentacion()))
                .toList();
        return new DatosDeEjercicio(peticion.enunciado(), peticion.imagenEnunciado(), peticion.idComponente(),
                peticion.idCompetencia(), peticion.idNivelDificultad(), opciones);
    }
}
