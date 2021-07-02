package no.nav.veilarbarena.service;

import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

import static java.lang.String.valueOf;
import static java.time.LocalDate.now;

@Service
public class MetricsService {

    private final MetricsClient metricsClient;

    @Autowired
    public MetricsService(MetricsClient metricsClient) {
        this.metricsClient = metricsClient;
    }

    public void leggerBrukerPaKafkaMetrikk(EndringPaaOppfoelgingsBrukerV1 bruker) {
        final ZonedDateTime iservFraDato = bruker.getIserv_fra_dato();
        boolean erIserv28 = false;

        if (iservFraDato != null) {
            erIserv28 = iservFraDato.toLocalDate().isBefore(now().minusDays(28));
        }

        Event event = new Event("bruker.kafka.send")
            .addTagToReport("formidlingsgruppekode", bruker.getFormidlingsgruppekode())
            .addTagToReport("kvalifiseringsgruppekode", bruker.getKvalifiseringsgruppekode())
            .addTagToReport("rettighetsgruppekode", bruker.getRettighetsgruppekode())
            .addFieldToReport("kontor", bruker.getNav_kontor())
            .addTagToReport("iserv28", valueOf(erIserv28))
            .addFieldToReport("endringstidspunkt", bruker.getEndret_dato().toInstant());

        metricsClient.report(event);
    }

}
