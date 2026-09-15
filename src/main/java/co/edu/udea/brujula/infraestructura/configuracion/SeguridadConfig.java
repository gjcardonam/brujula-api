package co.edu.udea.brujula.infraestructura.configuracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SeguridadConfig {

    private static final String[] RUTAS_PUBLICAS = {
            "/api/salud",
            "/api/auth/google",
            "/api/auth/google/config",
            "/api/auth/registro",
            "/api/auth/login",
            "/api/auth/recuperar",
            "/api/auth/restablecer/**",
            "/api/catalogos/publicos"
    };

    private final FiltroDeAutenticacion filtro;
    private final ObjectMapper json;
    private final BrujulaProperties propiedades;

    public SeguridadConfig(FiltroDeAutenticacion filtro, ObjectMapper json, BrujulaProperties propiedades) {
        this.filtro = filtro;
        this.json = json;
        this.propiedades = propiedades;
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(configuracionCors()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(rutas -> rutas
                    .requestMatchers(RUTAS_PUBLICAS).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/archivos/**").permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll())
            .exceptionHandling(errores -> errores
                    .authenticationEntryPoint((req, res, ex) -> responder(res, HttpServletResponse.SC_UNAUTHORIZED,
                            "SESION_INVALIDA", "Tu sesión no es válida o expiró. Vuelve a iniciar sesión."))
                    .accessDeniedHandler((req, res, ex) -> responder(res, HttpServletResponse.SC_FORBIDDEN,
                            "ACCESO_DENEGADO", "No tienes permisos para realizar esta acción.")))
            .addFilterBefore(filtro, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void responder(HttpServletResponse respuesta, int estado, String codigo, String mensaje) throws IOException {
        respuesta.setStatus(estado);
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        json.writeValue(respuesta.getWriter(), Map.of("estado", estado, "codigo", codigo, "mensaje", mensaje));
    }

    @Bean
    public CorsConfigurationSource configuracionCors() {
        CorsConfiguration configuracion = new CorsConfiguration();

        configuracion.setAllowedOriginPatterns(List.of(propiedades.frontendUrl(), "http://localhost:*",
                "http://127.0.0.1:*"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("*"));
        configuracion.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", configuracion);
        return fuente;
    }
}
