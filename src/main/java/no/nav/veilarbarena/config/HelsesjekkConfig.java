package no.nav.veilarbarena.config;

import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.health.selftest.SelfTestChecks;
import no.nav.common.health.selftest.SelfTestMeterBinder;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.utils.DatabaseUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

@Configuration
public class HelsesjekkConfig {

    @Bean
    public SelfTestChecks selfTestChecks(JdbcTemplate db, ArenaOrdsClient arenaOrdsClient) {
        List<SelfTestCheck> selfTestChecks = Arrays.asList(
                new SelfTestCheck("Arena ORDS ping", true, arenaOrdsClient),
                new SelfTestCheck("Database ping", true, () -> DatabaseUtils.checkDbHealth(db))
        );

        return new SelfTestChecks(selfTestChecks);
    }

    @Bean
    public SelfTestMeterBinder selfTestMeterBinder(SelfTestChecks selfTestChecks) {
        return new SelfTestMeterBinder(selfTestChecks);
    }
}
