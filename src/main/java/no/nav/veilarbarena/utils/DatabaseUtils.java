package no.nav.veilarbarena.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.health.HealthCheckResult;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
public class DatabaseUtils {

    public static HealthCheckResult checkDbHealth(JdbcTemplate db) {
        try {
            db.query("SELECT 1 FROM DUAL", resultSet -> {});
            return HealthCheckResult.healthy();
        } catch (Exception e) {
            log.error("Helsesjekk mot database feilet", e);
            return HealthCheckResult.unhealthy("Fikk ikke kontakt med databasen", e);
        }
    }

}
