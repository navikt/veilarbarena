package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.veilarbarena.domain.OppfolgingsbrukerSistEndret;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.domain.UserRecord;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerSistEndringRepository;
import no.nav.veilarbarena.service.KafkaService;
import no.nav.veilarbarena.service.SoapOppfolgingstatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

@Slf4j
@Component
public class OppfolgingsbrukerEndretSchedule {

    private final static long TEN_SECONDS = 10 * 1000;

    private final SoapOppfolgingstatusService soapOppfolgingstatusService;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    private final OppfolgingsbrukerSistEndringRepository oppfolgingsbrukerSistEndringRepository;

    private final KafkaService kafkaService;

    private final LeaderElectionClient leaderElectionClient;

    @Autowired
    public OppfolgingsbrukerEndretSchedule(
            SoapOppfolgingstatusService soapOppfolgingstatusService,
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            OppfolgingsbrukerSistEndringRepository oppfolgingsbrukerSistEndringRepository,
            KafkaService kafkaService, LeaderElectionClient leaderElectionClient
    ) {
        this.soapOppfolgingstatusService = soapOppfolgingstatusService;
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.oppfolgingsbrukerSistEndringRepository = oppfolgingsbrukerSistEndringRepository;
        this.kafkaService = kafkaService;
        this.leaderElectionClient = leaderElectionClient;
    }

    @Scheduled(fixedDelay = TEN_SECONDS, initialDelay = TEN_SECONDS)
    public void sendVedtakSendtFeiledeKafkaMeldinger() {
        if (leaderElectionClient.isLeader()) {
            publisereArenaBrukerEndringer();
        }
    }

    @Transactional
    void publisereArenaBrukerEndringer() {
        try {
            OppfolgingsbrukerSistEndret sistEndret = oppfolgingsbrukerSistEndringRepository.hentSistEndret();
            List<OppfolgingsbrukerDTO> users = oppfolgingsbrukerRepository.changesSinceLastCheckSql(sistEndret.getFodselsnr(), sistEndret.getOppfolgingsbrukerSistEndring());

            if (!users.isEmpty()) {
                OppfolgingsbrukerDTO sisteBruekr = users.get(users.size() - 1);
                oppfolgingsbrukerSistEndringRepository.updateLastcheck(sisteBruekr.getFodselsnr(), sisteBruekr.getEndret_dato());
            }

            log.info("Legger {} brukere til kafka", users.size());
            users.forEach(this::publish);
        } catch(Exception e) {
            log.error("Feil ved publisering av arena endringer til kafka", e);
        }
    }

    private void publish(OppfolgingsbrukerDTO oppfolgingsbrukerDTO) {
        soapOppfolgingstatusService.invalidateCachedUser(oppfolgingsbrukerDTO.getFodselsnr());
        kafkaService.sendBrukerEndret(oppfolgingsbrukerDTO);
    }

}
