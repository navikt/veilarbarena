package no.nav.fo.veilarbarena.scheduled;

import io.vavr.collection.List;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.UserRecord;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.mapping.QueryMapping;
import no.nav.sbl.sql.order.OrderClause;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static java.time.ZonedDateTime.now;

public class UserChangePublisher {
    private static final int SECONDS = 1000;
    static {
        QueryMapping.register(String.class, PersonId.AktorId.class, PersonId::aktorId);
        QueryMapping.register(String.class, PersonId.Fnr.class, PersonId::fnr);
    }

    @Inject
    private JdbcTemplate db;
    @Inject
    private java.util.List<UserChangeListener> listeners;
    private ZonedDateTime lastCheck = now().minusMinutes(10);

    @Scheduled(fixedDelay = 10 * SECONDS, initialDelay = SECONDS)
    private void findChangesSinceLastCheck() {
        WhereClause hasAktoerId = WhereClause.isNotNull("aktoerid");
        WhereClause updated = WhereClause.gteq("tidsstempel", Timestamp.from(lastCheck.toInstant()));

        List<User> users = List.ofAll(SqlUtils.select(db, "oppfolgingsbruker", UserRecord.class)
                .leftJoinOn("aktoerid_to_personid", "person_id", "personid")
                .where(hasAktoerId.and(updated))
                .orderBy(OrderClause.asc("tidsstempel"))
                .limit(1000)
                .executeToList())
                .map(User::of);

        users.forEach(this::publish);

        lastCheck = users.lastOption().map(User::getTidsstempel).getOrElse(now());
    }

    private void publish(User person) {
        this.listeners.forEach((listener) -> listener.userChanged(person));
    }
}
