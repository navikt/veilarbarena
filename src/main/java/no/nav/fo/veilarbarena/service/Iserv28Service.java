package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.Iserv28;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jose4j.json.internal.json_simple.JSONValue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static no.nav.fo.veilarbarena.config.KafkaConfig.ISERV28DAGER_TOPIC;
import static no.nav.fo.veilarbarena.config.KafkaConfig.createProducer;

public class Iserv28Service {

    private final JdbcTemplate jdbc;

    @Inject
    public Iserv28Service(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(cron = "* * * 10")
    public void sendTilKafkaBrukereMedIserv28Dager() {
        final Producer<String, String> producer = createProducer();
        finnBrukereMedIservI28Dager(ZonedDateTime.now())
                .forEach(iserv -> producer.send(new ProducerRecord<>(ISERV28DAGER_TOPIC, JSONValue.toJSONString(iserv))));
    }

    public List<Iserv28> finnBrukereMedIservI28Dager(ZonedDateTime oppdatertEtter) {
        return finnBrukereMedIservI28Dager(oppdatertEtter, 100);
    }

    public List<Iserv28> finnBrukereMedIservI28Dager(ZonedDateTime oppdatertEtter, int antall) {
        Timestamp tilbake28 = Timestamp.valueOf(LocalDateTime.now().minusDays(28));

        WhereClause erIserv = WhereClause.equals("formidlingsgruppekode", "ISERV");
        WhereClause harAktoerId = WhereClause.isNotNull("aktoerid");
        WhereClause erOppdatertSidenSist = WhereClause.gteq("tidsstempel", Timestamp.from(oppdatertEtter.toInstant()));
        WhereClause iservDato28DagerTilbake = WhereClause.lteq("iserv_fra_dato", tilbake28);

        return SqlUtils.select(jdbc, "oppfolgingsbruker", Iserv28Service::mapper)
                .column("tidsstempel")
                .column("aktoerid")
                .column("iserv_fra_dato")
                .leftJoinOn("aktoerid_to_personid", "person_id", "personid")
                .where(erIserv.and(harAktoerId).and(erOppdatertSidenSist).and(iservDato28DagerTilbake))
                .limit(antall)
                .executeToList();
    }

    private static Iserv28 mapper(ResultSet resultSet) throws SQLException {
        return new Iserv28(
                resultSet.getString("aktoerid"),
                resultSet.getTimestamp("tidsstempel").toLocalDateTime().atZone(ZoneId.systemDefault()),
                resultSet.getTimestamp("iserv_fra_dato").toLocalDateTime().atZone(ZoneId.systemDefault())
        );
    }
}
