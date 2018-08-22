package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.Bruker;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class BrukereMedOppdateringService {

    private final JdbcTemplate jdbc;

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;
    private final long TEN_SECONDS = 10000;
    @Inject
    public BrukereMedOppdateringService(JdbcTemplate jdbc, OppfolgingsbrukerEndringTemplate kafkaTemplate) {
        this.jdbc = jdbc;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = TEN_SECONDS, initialDelay = TEN_SECONDS)
    public void sendTilKafkaBrukereMedEndringerSiden() {
        finnBrukereMedEndringSiden(ZonedDateTime.now().minusSeconds(10)).forEach(kafkaTemplate::send);
    }

    public List<Bruker> finnBrukereMedEndringSiden(ZonedDateTime oppdatertEtter) {
        return finnBrukereMedEndringSiden(oppdatertEtter, 100);
    }

    public List<Bruker> finnBrukereMedEndringSiden(ZonedDateTime oppdatertEtter, int antall) {

        WhereClause harAktoerId = WhereClause.isNotNull("aktoerid");
        WhereClause erOppdatertSidenSist = WhereClause.gteq("tidsstempel", Timestamp.from(oppdatertEtter.toInstant()));

        return SqlUtils.select(jdbc, "oppfolgingsbruker", BrukereMedOppdateringService::mapper)
                .column("aktoerid")
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
                resultSet.getString("aktoerid"),
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
