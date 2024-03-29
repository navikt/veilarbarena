package no.nav.veilarbarena.config;

import no.nav.common.auth.context.UserRole;
import no.nav.common.test.auth.JavaxTestAuthContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.veilarbarena.utils.TestData.TEST_VEILEDER_IDENT;

@Configuration
public class FilterTestConfig {

    @Bean
    public FilterRegistrationBean testSubjectFilterRegistrationBean() {
        FilterRegistrationBean<JavaxTestAuthContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JavaxTestAuthContextFilter(UserRole.INTERN, TEST_VEILEDER_IDENT));
        registration.setOrder(1);
        registration.addUrlPatterns("/api/*");
        return registration;
    }

}
