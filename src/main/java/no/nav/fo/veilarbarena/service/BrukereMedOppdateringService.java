package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.Bruker;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static no.nav.fo.veilarbarena.config.KafkaConfig.OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN;
import static no.nav.fo.veilarbarena.config.KafkaConfig.createProducer;

public class BrukereMedOppdateringService {

    private final JdbcTemplate jdbc;

    @Inject
    public BrukereMedOppdateringService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Scheduled(fixedDelay = 10000)
    public void sendTilKafkaBrukereMedEndringerSiden() {
        final Producer<String, String> producer = createProducer();
        finnBrukereMedEndringSiden(ZonedDateTime.now())
                .forEach(bruker -> producer.send(new ProducerRecord<>(OPPFOLGINGSBRUKER_MED_ENDRING_SIDEN, JSONValue.toJSONString(bruker))));
    }

    public List<Bruker> finnBrukereMedEndringSiden(ZonedDateTime oppdatertEtter) {
        return finnBrukereMedEndringSiden(oppdatertEtter, 100);
    }

    public List<Bruker> finnBrukereMedEndringSiden(ZonedDateTime oppdatertEtter, int antall) {

        WhereClause harAktoerId = WhereClause.isNotNull("aktoerid");
        WhereClause erOppdatertSidenSist = WhereClause.gteq("tidsstempel", Timestamp.from(oppdatertEtter.toInstant()));

        return SqlUtils.select(jdbc, "oppfolgingsbruker", BrukereMedOppdateringService::mapper)
                .column("person_id")
                .column("aktoerid")
                .column("fodselsnr")
                .column("etternavn")
                .column("fornavn")
                .column("nav_kontor")
                .column("formidlingsgruppekode")
                .column("iserv_fra_dato")
                .column("kvalifiseringsgruppekode")
                .column("rettighetsgruppekode")
                .column("hovedmaalkode")
                .column("sikkerhetstiltak_type_kode")
                .column("fr_kode")
                .column("har_oppfolgingssak")
                .column("sperret_ansatt")
                .column("er_doed")
                .column("doed_fra_dato")
                .column("tidsstempel")
                .leftJoinOn("aktoerid_to_personid", "person_id", "personid")
                .where(harAktoerId.and(erOppdatertSidenSist))
                .limit(antall)
                .executeToList();
    }

    public static Bruker mapper(ResultSet resultSet) throws SQLException {
        return new Bruker(
                resultSet.getLong("person_id"),
                resultSet.getString("aktoerid"),
                resultSet.getString("fodselsnr"),
                resultSet.getString("etternavn"),
                resultSet.getString("fornavn"),
                resultSet.getString("nav_kontor"),
                resultSet.getString("formidlingsgruppekode"),
                resultSet.getTimestamp("iserv_fra_dato").toLocalDateTime().atZone(ZoneId.systemDefault()),
                resultSet.getString("kvalifiseringsgruppekode"),
                resultSet.getString("rettighetsgruppekode"),
                resultSet.getString("hovedmaalkode"),
                resultSet.getString("sikkerhetstiltak_type_kode"),
                resultSet.getString("fr_kode"),
                resultSet.getString("har_oppfolgingssak"),
                resultSet.getString("sperret_ansatt"),
                resultSet.getBoolean("er_doed"),
                resultSet.getTimestamp("doed_fra_dato").toLocalDateTime().atZone(ZoneId.systemDefault()),
                resultSet.getTimestamp("tidsstempel").toLocalDateTime().atZone(ZoneId.systemDefault())
        );
    }
}
