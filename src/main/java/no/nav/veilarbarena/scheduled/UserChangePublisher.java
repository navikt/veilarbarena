package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.veilarbarena.domain.FeiletKafkaBruker;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.domain.UserRecord;
import no.nav.veilarbarena.repository.KafkaRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Slf4j
@Component
public class UserChangePublisher {

    private final LeaderElectionClient leaderElectionClient;

    private final KafkaRepository kafkaRepository;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    private final AktorregisterClient aktorregisterClient;

    private static final String ARBEIDSOKER = "ARBS";
    private static final Set<String> OPPFOLGINGKODER = new HashSet<>(asList("BATT", "BFORM", "IKVAL", "VURDU", "OPPFI", "VARIG"));
    private static final String IKKE_ARBEIDSSOKER = "IARBS";

    @Autowired
    public UserChangePublisher(
            LeaderElectionClient leaderElectionClient, KafkaRepository kafkaRepository,
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository, AktorregisterClient aktorregisterClient
    ){
        this.leaderElectionClient = leaderElectionClient;
        this.kafkaRepository = kafkaRepository;
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.aktorregisterClient = aktorregisterClient;
    }

    @Scheduled(fixedDelay = 10_000L, initialDelay = 1000L)
    public void findChangesSinceLastCheck() {
        if (leaderElectionClient.isLeader()) {
            publisereArenaBrukerEndringer();
        }
    }

    @Transactional
    void publisereArenaBrukerEndringer(){
        try {
            List<User> users = changesSinceLastCheckSql();

            if (!users.isEmpty()) {
                User user = users.get(users.size() - 1);
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

        log.info("Siste sjekket tidspunkt: {} og aktorid: {}", sistSjekketTidspunkt, aktoerRegisterService.tilAktorId(getLastCheckFnr()));

        return List.ofAll(SqlUtils.select(db, "oppfolgingsbruker", UserRecord.class)
                .where(erUnderOppfolging().and(tidspunktEqualsOgFnr.or(tidspunktGreater)))
                .orderBy(OrderClause.asc("tidsstempel, fodselsnr"))
                .limit(1000)
                .executeToList())
                .map(User::of);
    }

    public List<User> findAllFailedKafkaUsers() {
        List<String> feiledeFnrs = kafkaRepository.hentFeiledeBrukere()
                .stream()
                .map(FeiletKafkaBruker::getFodselsnr)
                .collect(Collectors.toList());

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

    private WhereClause erUnderOppfolging() {
        WhereClause arbeidssoker = WhereClause.equals("FORMIDLINGSGRUPPEKODE", ARBEIDSOKER);
        WhereClause erIArbeidOgHarInnsatsbehov = WhereClause.equals("FORMIDLINGSGRUPPEKODE", IKKE_ARBEIDSSOKER)
                .and(WhereClause.in("KVALIFISERINGSGRUPPEKODE", OPPFOLGINGKODER));
        return arbeidssoker.or(erIArbeidOgHarInnsatsbehov);
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
