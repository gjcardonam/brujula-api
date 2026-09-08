package co.edu.udea.brujula.infraestructura.salida.persistencia.adaptador;

import co.edu.udea.brujula.dominio.modelo.Ejercicio;
import co.edu.udea.brujula.dominio.modelo.Opcion;
import co.edu.udea.brujula.dominio.modelo.Pagina;
import co.edu.udea.brujula.dominio.modelo.consulta.ComponentesDelBanco;
import co.edu.udea.brujula.dominio.puerto.salida.EjercicioRepositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.Mapeador;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.EjercicioEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.OpcionEntidad;
import co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class EjercicioAdaptador implements EjercicioRepositorio {

    private final EjercicioJpa ejercicios;
    private final ComponenteJpa componentes;
    private final CompetenciaJpa competencias;
    private final NivelDificultadJpa niveles;
    private final TipoErrorJpa tiposDeError;
    private final UsuarioJpa usuarios;

    public EjercicioAdaptador(EjercicioJpa ejercicios, ComponenteJpa componentes, CompetenciaJpa competencias,
                              NivelDificultadJpa niveles, TipoErrorJpa tiposDeError, UsuarioJpa usuarios) {
        this.ejercicios = ejercicios;
        this.componentes = componentes;
        this.competencias = competencias;
        this.niveles = niveles;
        this.tiposDeError = tiposDeError;
        this.usuarios = usuarios;
    }

    @Override
    public Pagina<Ejercicio> buscar(Long idComponente, boolean soloActivos, int pagina, int tamano) {
        Page<EjercicioEntidad> encontrados = ejercicios.listar(soloActivos, idComponente, PageRequest.of(pagina, tamano));
        return Pagina.de(encontrados.getContent().stream().map(Mapeador::aDominioSinOpciones).toList(),
                encontrados.getNumber(), encontrados.getSize(), encontrados.getTotalElements());
    }

    @Override
    public Optional<Ejercicio> porId(Long id) {
        return ejercicios.buscarCompleto(id).map(Mapeador::aDominio);
    }

    @Override
    public ComponentesDelBanco conteoPorComponente(boolean soloActivos) {
        List<ComponentesDelBanco.Conteo> conteos = ejercicios.contarPorComponente(soloActivos).stream()
                .map(fila -> new ComponentesDelBanco.Conteo((Long) fila[0], (String) fila[1], (Long) fila[2]))
                .toList();
        return new ComponentesDelBanco(ejercicios.contarTotal(soloActivos), conteos);
    }

    @Override
    public boolean existeOtroConEnunciado(String enunciado, Long idExcluido) {
        return ejercicios.idsConEnunciado(enunciado).stream()
                .anyMatch(id -> idExcluido == null || !id.equals(idExcluido));
    }

    @Override
    public long cantidadDeActivos() {
        return ejercicios.countByEstado(Ejercicio.ACTIVO);
    }

    @Override
    public Optional<Long> siguienteParaPractica(Long idActual, Long idComponente, Long idEstudiante) {
        return ejercicios.siguienteParaPractica(idActual, idComponente, idEstudiante);
    }

    @Override
    public Optional<Long> siguienteParaSimulacro(Long idSimulacro) {
        return ejercicios.siguienteParaSimulacro(idSimulacro);
    }

    @Override
    public Ejercicio guardar(Ejercicio ejercicio) {
        EjercicioEntidad entidad = ejercicio.id() == null
                ? nueva(ejercicio)
                : ejercicios.buscarCompleto(ejercicio.id()).orElseThrow();

        entidad.setEnunciado(ejercicio.enunciado());
        entidad.setImagen(ejercicio.imagen());
        entidad.setEstado(ejercicio.estado());
        entidad.setComponente(componentes.findById(ejercicio.componente().id()).orElseThrow());
        entidad.setCompetencia(competencias.findById(ejercicio.competencia().id()).orElseThrow());
        entidad.setNivel(niveles.findById(ejercicio.nivel().id()).orElseThrow());
        sincronizarOpciones(entidad, ejercicio.opciones());

        EjercicioEntidad guardado = ejercicios.saveAndFlush(entidad);
        ejercicio.asignarId(guardado.getId());
        return Mapeador.aDominio(guardado);
    }

    private EjercicioEntidad nueva(Ejercicio ejercicio) {
        EjercicioEntidad entidad = new EjercicioEntidad();
        entidad.setCreadoEn(ejercicio.creadoEn());
        entidad.setCreador(usuarios.getReferenceById(ejercicio.idCreador()));
        return entidad;
    }

    /**
     * Reescribe la lista de opciones respetando los identificadores de las que ya existían, para no
     * romper los intentos que apuntan a ellas. Las que desaparecen las borra JPA por orphanRemoval.
     */
    private void sincronizarOpciones(EjercicioEntidad entidad, List<Opcion> opciones) {
        Map<Long, OpcionEntidad> existentes = new HashMap<>();
        entidad.getOpciones().forEach(o -> existentes.put(o.getId(), o));

        List<OpcionEntidad> resultado = new ArrayList<>();
        for (Opcion opcion : opciones) {
            OpcionEntidad destino = opcion.id() == null ? new OpcionEntidad() : existentes.get(opcion.id());
            if (destino == null) {
                throw new IllegalArgumentException("La opción " + opcion.id() + " no pertenece al ejercicio");
            }
            destino.setEjercicio(entidad);
            destino.setTexto(opcion.texto());
            destino.setImagen(opcion.imagen());
            destino.setCorrecta(opcion.correcta());
            destino.setRetroalimentacion(opcion.retroalimentacion());
            destino.setOrden(opcion.orden());
            destino.setTipoError(opcion.tipoError() == null ? null : tiposDeError.findById(opcion.tipoError().id()).orElseThrow());
            resultado.add(destino);
        }
        entidad.getOpciones().clear();
        entidad.getOpciones().addAll(resultado);
    }
}
