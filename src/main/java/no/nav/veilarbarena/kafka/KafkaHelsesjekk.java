package no.nav.veilarbarena.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
public class KafkaHelsesjekk implements HealthCheck {

    private final JdbcTemplate db;

    @Autowired
    public KafkaHelsesjekk(JdbcTemplate db) {
        this.db = db;
    }

    @Override
    public HealthCheckResult checkHealth() {
        long antallFeiledeBrukere = db.queryForObject("SELECT COUNT(*) FROM FEILEDE_KAFKA_BRUKERE", Long.class);
        if (antallFeiledeBrukere > 0) {
            return HealthCheckResult.unhealthy(format("Det ligger %d feilede brukere i databasen", antallFeiledeBrukere));
        }

        return HealthCheckResult.healthy();
    }
}


