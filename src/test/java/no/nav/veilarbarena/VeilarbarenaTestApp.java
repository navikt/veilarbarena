package no.nav.veilarbarena;

import no.nav.veilarbarena.config.ApplicationTestConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Import;

@EnableAutoConfiguration(exclude = {CompositeMeterRegistryAutoConfiguration.class})
@ServletComponentScan
@Import(ApplicationTestConfig.class)
public class VeilarbarenaTestApp {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(VeilarbarenaTestApp.class);
        application.setAdditionalProfiles("local");
        application.run(args);
    }

}
