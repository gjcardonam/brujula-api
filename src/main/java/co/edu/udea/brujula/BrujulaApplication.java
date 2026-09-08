package co.edu.udea.brujula;

import co.edu.udea.brujula.config.BrujulaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(BrujulaProperties.class)
public class BrujulaApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrujulaApplication.class, args);
    }
}
