package no.nav.fo.veilarbarena.config;

import no.nav.apiapp.ApiApplication;
import no.nav.apiapp.config.ApiAppConfigurator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        DbConfig.class,
        FeedConfig.class
})
@ComponentScan("no.nav.fo.veilarbarena.rest")
public class ApplicationConfig implements ApiApplication.NaisApiApplication {
    public static final String APPLICATION_NAME = "veilarbarena";
    public static final String REDIRECT_URL_PROPERTY = "VEILARBLOGIN_REDIRECT_URL_URL";

    @Override
    public void configure(ApiAppConfigurator apiAppConfigurator) {
    }
}
