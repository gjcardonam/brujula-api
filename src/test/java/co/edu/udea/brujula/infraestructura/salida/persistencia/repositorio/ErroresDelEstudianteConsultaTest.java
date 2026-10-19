package co.edu.udea.brujula.infraestructura.salida.persistencia.repositorio;
import co.edu.udea.brujula.infraestructura.salida.persistencia.entidad.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import static org.junit.jupiter.api.Assertions.*;
class ErroresDelEstudianteConsultaTest {
    @Test void consultaEsValidaParaLosMapeosRealesYFiltraPorEstudiante() throws Exception {
        var registro = new StandardServiceRegistryBuilder()
            .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            .applySetting("hibernate.boot.allow_jdbc_metadata_access", "false")
            .applySetting("hibernate.hbm2ddl.auto", "none").build();
        try {
            var fuentes = new MetadataSources(registro);
            for (var entidad : new Class<?>[]{IntentoEntidad.class, EjercicioEntidad.class, OpcionEntidad.class,
                    TipoErrorEntidad.class, ComponenteEntidad.class, CompetenciaEntidad.class,
                    NivelDificultadEntidad.class, UsuarioEntidad.class, RolEntidad.class}) fuentes.addAnnotatedClass(entidad);
            try (var fabrica = fuentes.buildMetadata().buildSessionFactory(); var sesion = fabrica.openSession()) {
                String hql = IntentoJpa.class.getMethod("erroresDelEstudiante", Long.class).getAnnotation(Query.class).value();
                var consulta = sesion.createQuery(hql, Object[].class);
                assertDoesNotThrow(() -> consulta.setParameter("idUsuario", 7L));
                assertTrue(hql.contains("left join i.tipoError"));
            }
        } finally { StandardServiceRegistryBuilder.destroy(registro); }
    }
}
