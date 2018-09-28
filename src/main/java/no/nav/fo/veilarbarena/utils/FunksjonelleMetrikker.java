package no.nav.fo.veilarbarena.utils;

import no.nav.fo.veilarbarena.domain.User;
import no.nav.metrics.MetricsFactory;

import java.time.ZonedDateTime;

import static java.lang.String.valueOf;
import static java.time.LocalDate.now;

public class FunksjonelleMetrikker {

    public static void leggerBrukerPaKafkaMetrikk(User user) {
        final ZonedDateTime iservFraDato = user.getIserv_fra_dato();
        boolean erIserv28 = false;

        if (iservFraDato != null) {
            erIserv28 = iservFraDato.toLocalDate().isBefore(now().minusDays(28));
        }

        MetricsFactory.createEvent("bruker.kafka.send")
                .addTagToReport("formidlingsgruppekode", user.getFormidlingsgruppekode())
                .addTagToReport("kvalifiseringsgruppekode", user.getKvalifiseringsgruppekode())
                .addTagToReport("rettighetsgruppekode", user.getRettighetsgruppekode())
                .addFieldToReport("kontor",  user.getNav_kontor())
                .addTagToReport("iserv28", valueOf(erIserv28))
                .addFieldToReport("endringstidspunkt", user.getEndret_dato().toInstant())
                .report();
    }

    public static void feilVedSendingTilKafka() {
        MetricsFactory.createEvent("bruker.kafka.send.feil").report();
    }
}
