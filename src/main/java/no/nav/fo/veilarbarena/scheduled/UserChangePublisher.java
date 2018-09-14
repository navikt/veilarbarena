package no.nav.fo.veilarbarena.scheduled;

import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.User;
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

@Slf4j
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
        List<User> users = changesSinceLastCheckSql();

        if (users.isEmpty()) {
            updateLastcheck(now(), "");
        } else {
            User user = users.lastOption().get();
            updateLastcheck(user.getTidsstempel(), user.getFodselsnr().get());
            log.info("Legger {} brukere til kafka", users.size());
        }

        users.forEach(this::publish);
    }

    private List<User> changesSinceLastCheckSql() {
        final ZonedDateTime sistSjekketTidspunkt = getLastCheck();

        WhereClause tidspunktEquals = WhereClause.equals("tidsstempel", Timestamp.from(sistSjekketTidspunkt.toInstant()));
        WhereClause tidspunktGreater = WhereClause.gt("tidsstempel", Timestamp.from(sistSjekketTidspunkt.toInstant()));
        WhereClause sistSjekketFnrGreater = WhereClause.gt("fodselsnr", getLastCheckFnr());

        WhereClause tidspunktEqualsOgFnr = tidspunktEquals.and(sistSjekketFnrGreater);

        return List.ofAll(SqlUtils.select(db, "oppfolgingsbruker", UserRecord.class)
                .where(tidspunktEqualsOgFnr.or(tidspunktGreater))
                .orderBy(OrderClause.asc("tidsstempel, fodselsnr"))
                .limit(1000)
                .executeToList())
                .map(User::of);
    }

    private void updateLastcheck(ZonedDateTime tidspunkt, String fnr) {
        SqlUtils.update(db, "METADATA")
                .set("OPPFOLGINGSBRUKER_SIST_ENDRING", Timestamp.from(tidspunkt.toInstant()))
                .set("FODSELSNR", fnr)
                .execute();
    }

    private ZonedDateTime getLastCheck() {
        return SqlUtils.select(db, "METADATA", SisteOppdatertRecord.class)
                .execute()
                .getOppfolgingsbruker_sist_endring()
                .value;
    }

    private String getLastCheckFnr() {
        String fnrFraDb = SqlUtils.select(db, "METADATA", SisteOppdatertRecord.class)
                .execute()
                .getFodselsnr()
                .value;

        if (fnrFraDb == null) {
            return "";
        }
        return fnrFraDb;
    }

    private void publish(User person) {
        this.listeners.forEach((listener) -> listener.userChanged(person));
    }
}
