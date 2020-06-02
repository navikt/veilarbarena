package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.veilarbarena.domain.Oppfolgingsbruker;
import no.nav.veilarbarena.domain.OppfolgingsbrukerSistEndret;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerEndretDTO;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerSistEndringRepository;
import no.nav.veilarbarena.service.KafkaService;
import no.nav.veilarbarena.service.SoapOppfolgingstatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            List<Oppfolgingsbruker> brukere = oppfolgingsbrukerRepository.changesSinceLastCheckSql(
                    sistEndret.getFodselsnr(), sistEndret.getOppfolgingsbrukerSistEndring()
            );

            if (!brukere.isEmpty()) {
                Oppfolgingsbruker sisteBruker = brukere.get(brukere.size() - 1);
                oppfolgingsbrukerSistEndringRepository.updateLastcheck(sisteBruker.getFodselsnr(), sisteBruker.getTimestamp());
                log.info("Legger {} brukere til kafka", brukere.size());
                brukere.forEach(bruker -> {
                    soapOppfolgingstatusService.invalidateCachedUser(bruker.getFodselsnr());
                    kafkaService.sendBrukerEndret(OppfolgingsbrukerEndretDTO.fraOppfolgingsbruker(bruker));
                });
            } else {
                log.info("Ingen nye endringer å publisere på kafka");
            }
        } catch(Exception e) {
            log.error("Feil ved publisering av arena endringer til kafka", e);
        }
    }

}