package no.nav.fo.veilarbarena.scheduled;

import io.vavr.collection.List;
import no.nav.fo.veilarbarena.DateUtils;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.order.OrderClause;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.function.Consumer;

import static java.time.ZonedDateTime.now;
import static no.nav.fo.veilarbarena.DateUtils.timestampToZonedDateTime;

public class UserChangePublisher {
    @Inject
    private JdbcTemplate db;
    private ZonedDateTime lastCheck = now().minusMinutes(10);
    private List<Consumer<User>> jobs;

    public void subscribe(Consumer<User> job) {
        this.jobs = this.jobs.append(job);
    }

    @Scheduled(fixedDelay = 10 * 1000)
    private void findChangesSinceLastCheck() {
        WhereClause hasAktoerId = WhereClause.isNotNull("aktoerid");
        WhereClause updated = WhereClause.gteq("tidsstempel", Timestamp.from(lastCheck.toInstant()));

        List<User> users = List.ofAll(SqlUtils.select(db, "oppfolgingsbruker", UserChangePublisher::mapper)
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
                .where(hasAktoerId.and(updated))
                .orderBy(OrderClause.asc("tidsstempel"))
                .limit(1000)
                .executeToList());

        users.forEach(this::publish);

        lastCheck = users.lastOption().map(User::getTidsstempel).getOrElse(now());
    }

    private void publish(User person) {
        this.jobs.forEach((job) -> job.accept(person));
    }

    public static User mapper(ResultSet resultSet) throws SQLException {
        return new User(
                PersonId.aktorId(resultSet.getString("aktoerid")),
                PersonId.fnr(resultSet.getString("fodselsnr")),
                resultSet.getString("etternavn"),
                resultSet.getString("fornavn"),
                resultSet.getString("nav_kontor"),
                resultSet.getString("formidlingsgruppekode"),
                timestampToZonedDateTime(resultSet.getTimestamp("iserv_fra_dato")),
                resultSet.getString("kvalifiseringsgruppekode"),
                resultSet.getString("rettighetsgruppekode"),
                resultSet.getString("hovedmaalkode"),
                resultSet.getString("sikkerhetstiltak_type_kode"),
                resultSet.getString("fr_kode"),
                resultSet.getString("har_oppfolgingssak"),
                resultSet.getString("sperret_ansatt"),
                resultSet.getBoolean("er_doed"),
                timestampToZonedDateTime(resultSet.getTimestamp("doed_fra_dato")),
                timestampToZonedDateTime(resultSet.getTimestamp("tidsstempel"))
        );
    }
}
