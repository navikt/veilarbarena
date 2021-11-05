package no.nav.veilarbarena.service;

import no.nav.common.metrics.Event;
import no.nav.common.metrics.MetricsClient;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV1;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public void leggerBrukerPaKafkaMetrikk(EndringPaaOppfoelgingsBrukerV2 bruker) {
        final LocalDate iservFraDato = bruker.getIservFraDato();
        boolean erIserv28 = false;

        if (iservFraDato != null) {
            erIserv28 = iservFraDato.isBefore(now().minusDays(28));
        }

        Event event = new Event("bruker.kafka.send")
            .addTagToReport("formidlingsgruppekode", bruker.getFormidlingsgruppe().name())
            .addTagToReport("kvalifiseringsgruppekode", bruker.getKvalifiseringsgruppe().name())
            .addTagToReport("rettighetsgruppekode", bruker.getRettighetsgruppe().name())
            .addFieldToReport("kontor", bruker.getOppfolgingsenhet())
            .addTagToReport("iserv28", valueOf(erIserv28))
            .addFieldToReport("endringstidspunkt", bruker.getSistEndretDato().toInstant());

        metricsClient.report(event);
    }

}
