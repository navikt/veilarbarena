package no.nav.veilarbarena.utils;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.health.HealthCheckResult;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

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

    public static String toSqlStringArray(List<String> strs) {
        if (strs.size() > 1000) {
            throw new IllegalArgumentException("Oracle databases cannot handle more than 1000 valus");
        }

        String values = strs.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));
        return "(" + values + ")";
    }

}
