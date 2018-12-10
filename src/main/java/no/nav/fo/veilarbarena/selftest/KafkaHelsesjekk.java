package no.nav.fo.veilarbarena.selftest;

import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class KafkaHelsesjekk implements Helsesjekk {

    @Inject
    private JdbcTemplate db;

    @Override
    public void helsesjekk() throws Throwable {
        if (db.queryForObject("SELECT COUNT(*) FROM FEILEDE_KAFKA_BRUKERE", Long.class) > 0) {
            throw new IllegalStateException();
        }
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata("kafka_status", "N/A", "Sjekker at det ikke er noen feil med sending av brukeroppdateringer til kafka", false);
    }
}
