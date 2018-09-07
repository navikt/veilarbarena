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
import org.springframework.transaction.annotation.Transactional;

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

    @Scheduled(fixedDelay = 10 * SECONDS, initialDelay = SECONDS)
    @Transactional
    void findChangesSinceLastCheck() {
        WhereClause updated = WhereClause.gteq("tidsstempel", Timestamp.from(getLastcheck().toInstant()));

        List<User> users = List.ofAll(SqlUtils.select(db, "oppfolgingsbruker", UserRecord.class)
                .where(updated)
                .orderBy(OrderClause.asc("tidsstempel"))
                .limit(1000)
                .executeToList())
                .map(User::of);

        updateLastcheck(users.lastOption().map(User::getTidsstempel).getOrElse(now()));

        users.forEach(this::publish);
    }

    private void updateLastcheck(ZonedDateTime time) {
        SqlUtils.update(db, "METADATA")
                .set("OPPFOLGINGSBRUKER_SIST_ENDRING", Timestamp.from(time.toInstant()))
                .execute();
    }

    private ZonedDateTime getLastcheck() {
        return SqlUtils.select(db, "METADATA", SisteOppdatertRecord.class)
                .execute()
                .getOppfolgingsbruker_sist_endring()
                .value;
    }

    private void publish(User person) {
        this.listeners.forEach((listener) -> listener.userChanged(person));
    }
}
