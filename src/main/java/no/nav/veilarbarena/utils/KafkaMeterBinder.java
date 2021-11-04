package no.nav.veilarbarena.utils;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import no.nav.veilarbarena.repository.entity.OppdatertBrukerEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.ZonedDateTime;

@Component
public class KafkaMeterBinder implements MeterBinder {

    private final OppdaterteBrukereRepository oppdaterteBrukereRepository;

    public KafkaMeterBinder(OppdaterteBrukereRepository oppdaterteBrukereRepository) {
        this.oppdaterteBrukereRepository = oppdaterteBrukereRepository;
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        Gauge.builder("delay_veialarbarena_sekunder", this::hentDelayISekunder).register(registry);
    }

    public long hentDelayISekunder() {
        OppdatertBrukerEntity oppdatertBrukerEntity = oppdaterteBrukereRepository.hentBrukereMedEldstEndring();
        if (oppdatertBrukerEntity == null) {
            return 0;
        }
        return Math.max(Date.from(ZonedDateTime.now().toInstant()).getTime() - oppdatertBrukerEntity.getTidsstempel().getTime(), 0);
    }

}