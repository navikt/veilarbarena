package no.nav.fo.veilarbarena.scheduled;

import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.domain.UserRecord;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringRepository;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.mapping.QueryMapping;
import no.nav.sbl.sql.order.OrderClause;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;

@Slf4j
public class UserChangePublisher {
    static {
        QueryMapping.register(String.class, PersonId.AktorId.class, PersonId::aktorId);
        QueryMapping.register(String.class, PersonId.Fnr.class, PersonId::fnr);
    }

    @Inject
    private JdbcTemplate db;
    @Inject
    private java.util.List<UserChangeListener> listeners;
    @Inject
    private OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository;
    @Inject
    private AktorService aktorService;

    private LockingTaskExecutor taskExecutor;
    private static final int lockAutomatiskAvslutteOppfolgingSeconds = 3600;

    public UserChangePublisher(LockingTaskExecutor taskExecutor){
        this.taskExecutor = taskExecutor;
    }

    @Scheduled(fixedDelay = 10000L, initialDelay = 1000L)
    public void findChangesSinceLastCheck() {
        Instant lockAtMostUntil = Instant.now().plusSeconds(lockAutomatiskAvslutteOppfolgingSeconds);
        Instant lockAtLeastUntil = Instant.now().plusSeconds(10);

        taskExecutor.executeWithLock(
                this::publisereArenaBrukerEndringer,
                new LockConfiguration("produserArenaBrukerEndringer", lockAtMostUntil, lockAtLeastUntil)
        );
    }

    @Transactional
    void publisereArenaBrukerEndringer(){
        try {
            List<User> users = changesSinceLastCheckSql();

            if (!users.isEmpty()) {
                User user = users.lastOption().get();
                updateLastcheck(user.getEndret_dato(), user.getFodselsnr().get());
            }

            List<User> feiledeFnrs = findAllFailedKafkaUsers();
            if (feiledeFnrs.isEmpty()) {
                log.info("Legger {} brukere til kafka", users.size());
                users.forEach(this::publish);
            } else {
                List<User> mergedUserList = users.appendAll(feiledeFnrs);
                log.info("Legger {} brukere som også inneholder feilede brukere til kafka", mergedUserList.size());
                mergedUserList.forEach(this::publish);
            }
        }
        catch(Exception e) {
            log.error("Feil ved publisering av arena endringer til kafka", e);
        }
    }

    private List<User> changesSinceLastCheckSql() {
        final ZonedDateTime sistSjekketTidspunkt = getLastCheck();

        WhereClause tidspunktEquals = WhereClause.equals("tidsstempel", Timestamp.from(sistSjekketTidspunkt.toInstant()));
        WhereClause tidspunktGreater = WhereClause.gt("tidsstempel", Timestamp.from(sistSjekketTidspunkt.toInstant()));
        WhereClause sistSjekketFnrGreater = WhereClause.gt("fodselsnr", getLastCheckFnr());

        WhereClause tidspunktEqualsOgFnr = tidspunktEquals.and(sistSjekketFnrGreater);

        log.info("Siste sjekket tidspunkt: {} og aktorid: {}", sistSjekketTidspunkt, aktorService.getAktorId(getLastCheckFnr()));

        return List.ofAll(SqlUtils.select(db, "oppfolgingsbruker", UserRecord.class)
                .where(tidspunktEqualsOgFnr.or(tidspunktGreater))
                .orderBy(OrderClause.asc("tidsstempel, fodselsnr"))
                .limit(1000)
                .executeToList())
                .map(User::of);
    }

    public List<User> findAllFailedKafkaUsers() {
        List<String> feiledeFnrs = oppfolgingsbrukerEndringRepository.hentFeiledeBrukere()
                .map(feiletBruker -> feiletBruker.getFodselsnr().value);

        log.info("Antall FeiledeFnr: {}", feiledeFnrs.size());

        if (!feiledeFnrs.isEmpty()) {
            return List.ofAll(SqlUtils.select(db, "OPPFOLGINGSBRUKER", UserRecord.class)
                    .where(WhereClause.in("FODSELSNR", feiledeFnrs.asJava()))
                    .limit(1000)
                    .executeToList())
                    .map(User::of);
        }
        return List.empty();
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
