package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.sbl.sql.DbConstants;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.config.KafkaConfig.KAFKA_TOPIC;
import static no.nav.fo.veilarbarena.utils.FunksjonelleMetrikker.feilVedSendingTilKafka;
import static no.nav.fo.veilarbarena.utils.FunksjonelleMetrikker.leggerBrukerPaKafkaMetrikk;
import static no.nav.json.JsonUtils.toJson;


@Slf4j
public class OppfolgingsbrukerEndringTemplate {
    private KafkaTemplate<String, String> kafkaTemplate;

    @Inject
    private JdbcTemplate db;

    @Inject
    public OppfolgingsbrukerEndringTemplate(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    void send(User user) {
        final String serialisertBruker = toJson(user);

        kafkaTemplate.send(
                KAFKA_TOPIC,
                user.getAktoerid().get(),
                serialisertBruker
        ).addCallback(
                sendResult -> onSuccess(user),
                throwable -> onError(throwable, user)
        );

        log.debug("Bruker: {} har endringer, legger på kafka", user.getAktoerid().get());
    }

    private void onSuccess(User user) {
        leggerBrukerPaKafkaMetrikk(user);
        deleteFeiletBruker(user);
    }

    private void onError(Throwable throwable, User user) {
        log.error("Kunne ikke publisere melding til kafka-topic", throwable);
        feilVedSendingTilKafka();
        insertFeiletBruker(user);
    }

    private void insertFeiletBruker(User user) {
        SqlUtils.insert(db, "FEILEDE_KAFKA_BRUKERE")
                .value("FODSELSNR", user.getFodselsnr().get())
                .value("TIDSPUNKT_FEILET", DbConstants.CURRENT_TIMESTAMP)
                .execute();
    }

    private void deleteFeiletBruker(User user) {
        SqlUtils.delete(db, "FEILEDE_KAFKA_BRUKERE")
                .where(WhereClause.equals("FODSELSNR", user.getFodselsnr().get()))
                .execute();
    }
}
