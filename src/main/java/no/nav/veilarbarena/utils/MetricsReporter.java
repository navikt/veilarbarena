package no.nav.veilarbarena.utils;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import no.nav.veilarbarena.repository.entity.OppdatertBrukerEntity;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.ZonedDateTime;

import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;

@Component
public class MetricsReporter {
    private static MeterRegistry prometheusMeterRegistry = new ProtectedPrometheusMeterRegistry();

    private final OppdaterteBrukereRepository oppdaterteBrukereRepository;

    public MetricsReporter(OppdaterteBrukereRepository oppdaterteBrukereRepository) {
        this.oppdaterteBrukereRepository = oppdaterteBrukereRepository;

        Gauge.builder("delay_veialarbarena_millisekunder", this::hentDelayIMillisekunder).register(getMeterRegistry());
    }

    public long hentDelayIMillisekunder() {
        OppdatertBrukerEntity oppdatertBrukerEntity = oppdaterteBrukereRepository.hentBrukereMedEldstEndring();
        if (oppdatertBrukerEntity == null) {
            return 0;
        }
        return Math.max(Date.from(ZonedDateTime.now().toInstant()).getTime() - oppdatertBrukerEntity.getTidsstempel().getTime(), 0);
    }

    public static MeterRegistry getMeterRegistry() {
        return prometheusMeterRegistry;
    }

    public static class ProtectedPrometheusMeterRegistry extends PrometheusMeterRegistry {
        public ProtectedPrometheusMeterRegistry() {
            super(DEFAULT);
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException();
        }
    }

}
