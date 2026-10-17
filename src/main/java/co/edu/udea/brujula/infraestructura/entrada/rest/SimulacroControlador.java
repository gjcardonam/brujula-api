package co.edu.udea.brujula.infraestructura.entrada.rest;

import co.edu.udea.brujula.dominio.modelo.consulta.EstadoDelSimulacro;
import co.edu.udea.brujula.dominio.modelo.consulta.ResumenDeSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarEstadoDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarResultadoDelSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ConsultarSimulacroEnCurso;
import co.edu.udea.brujula.dominio.puerto.entrada.FinalizarSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.IniciarSimulacro;
import co.edu.udea.brujula.dominio.puerto.entrada.ListarSimulacros;
import co.edu.udea.brujula.dominio.puerto.entrada.PresentarSiguienteEjercicio;
import co.edu.udea.brujula.dominio.puerto.entrada.ValidarSesion;
import co.edu.udea.brujula.dominio.puerto.salida.AlmacenDeImagenes;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Peticiones.SimulacroRequest;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.PaginaDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.ResultadoSimulacroDto;
import co.edu.udea.brujula.infraestructura.entrada.rest.dto.Respuestas.SiguienteSimulacroDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/simulacros")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class SimulacroControlador {

    private final IniciarSimulacro inicio;
    private final ConsultarSimulacroEnCurso enCurso;
    private final ListarSimulacros listado;
    private final ConsultarEstadoDelSimulacro estado;
    private final PresentarSiguienteEjercicio siguiente;
    private final FinalizarSimulacro finalizacion;
    private final ConsultarResultadoDelSimulacro resultado;
    private final AlmacenDeImagenes imagenes;

    public SimulacroControlador(IniciarSimulacro inicio, ConsultarSimulacroEnCurso enCurso, ListarSimulacros listado,
                                ConsultarEstadoDelSimulacro estado, PresentarSiguienteEjercicio siguiente,
                                FinalizarSimulacro finalizacion, ConsultarResultadoDelSimulacro resultado,
                                AlmacenDeImagenes imagenes) {
        this.inicio = inicio;
        this.enCurso = enCurso;
        this.listado = listado;
        this.estado = estado;
        this.siguiente = siguiente;
        this.finalizacion = finalizacion;
        this.resultado = resultado;
        this.imagenes = imagenes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstadoDelSimulacro iniciar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                      @RequestBody SimulacroRequest peticion) {
        return inicio.iniciar(usuario.id(), peticion.idDuracion());
    }

    @GetMapping("/en-curso")
    public ResponseEntity<EstadoDelSimulacro> enCurso(@AuthenticationPrincipal ValidarSesion.Autenticado usuario) {
        return enCurso.consultar(usuario.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/mios")
    public PaginaDto<ResumenDeSimulacro> mios(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                              @RequestParam(defaultValue = "0") int pagina) {
        return PaginaDto.de(listado.listar(usuario.id(), pagina));
    }

    @GetMapping("/{id}")
    public EstadoDelSimulacro estado(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                     @PathVariable Long id) {
        return estado.consultar(usuario.id(), id);
    }

    @GetMapping("/{id}/siguiente-ejercicio")
    public SiguienteSimulacroDto siguienteEjercicio(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                                    @PathVariable Long id) {
        return SiguienteSimulacroDto.de(siguiente.presentar(usuario.id(), id), imagenes);
    }

    @PostMapping("/{id}/finalizar")
    public ResultadoSimulacroDto finalizar(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                           @PathVariable Long id) {
        return ResultadoSimulacroDto.de(finalizacion.finalizar(usuario.id(), id), imagenes);
    }

    @GetMapping("/{id}/resultado")
    public ResultadoSimulacroDto resultado(@AuthenticationPrincipal ValidarSesion.Autenticado usuario,
                                           @PathVariable Long id) {
        return ResultadoSimulacroDto.de(resultado.consultar(usuario.id(), id), imagenes);
    }
}
