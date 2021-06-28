package no.nav.veilarbarena.config;

import no.finn.unleash.UnleashContext;
import no.nav.common.abac.AbacClient;
import no.nav.common.abac.Pep;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.AuthContextHolderThreadLocal;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.featuretoggle.UnleashClient;
import no.nav.common.featuretoggle.UnleashClientImpl;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.metrics.MetricsClient;
import no.nav.common.utils.Credentials;
import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.kafka.KafkaTopics;
import no.nav.veilarbarena.mock.AbacClientMock;
import no.nav.veilarbarena.mock.AktorregisterClientMock;
import no.nav.veilarbarena.mock.MetricsClientMock;
import no.nav.veilarbarena.mock.PepMock;
import no.nav.veilarbarena.utils.LocalH2Database;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableConfigurationProperties({EnvironmentProperties.class})
@Import({
        SwaggerConfig.class,
        ControllerTestConfig.class,
        RepositoryTestConfig.class,
        ServiceTestConfig.class,
        KafkaTestConfig.class,
        FilterTestConfig.class,
        HelsesjekkConfig.class
})
public class ApplicationTestConfig {

    @Bean
    public AuthContextHolder authContextHolder() {
        return AuthContextHolderThreadLocal.instance();
    }

    @Bean
    public Credentials serviceUserCredentials() {
        return new Credentials("username", "password");
    }

    @Bean
    public AktorOppslagClient aktorOppslagClient() {
        return new AktorregisterClientMock();
    }

    @Bean
    public AbacClient abacClient() {
        return new AbacClientMock();
    }

    @Bean
    public Pep veilarbPep(AbacClient abacClient) {
        return new PepMock(abacClient);
    }

    @Bean
    public MetricsClient metricsClient() {
        return new MetricsClientMock();
    }

    @Bean
    public DataSource dataSource() {
        return LocalH2Database.getDb().getDataSource();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return LocalH2Database.getDb();
    }

    @Bean
    public ArenaOrdsClient arenaOrdsClient() {
        return new ArenaOrdsClient() {
            @Override
            public <T> Optional<T> get(String path, String fnr, Class<T> clazz) {
                return Optional.empty();
            }

            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }
        };
    }

    @Bean
    public UnleashClient unleashClient() {
        return new UnleashClient() {
            @Override
            public HealthCheckResult checkHealth() {
                return HealthCheckResult.healthy();
            }

            @Override
            public boolean isEnabled(String s) {
                return true;
            }

            @Override
            public boolean isEnabled(String s, UnleashContext unleashContext) {
                return true;
            }
        };
    }

}
