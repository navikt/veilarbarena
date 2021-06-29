package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerEndretDTO;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerSistEndringRepository;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerSistEndretEntity;
import no.nav.veilarbarena.service.KafkaService;
import no.nav.veilarbarena.service.UnleashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class OppfolgingsbrukerEndretSchedule {

    private final static long TEN_SECONDS = 10 * 1000;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    private final OppfolgingsbrukerSistEndringRepository oppfolgingsbrukerSistEndringRepository;

    private final KafkaService kafkaService;

    private final LeaderElectionClient leaderElectionClient;

    private final UnleashService unleashService;

    @Autowired
    public OppfolgingsbrukerEndretSchedule(
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            OppfolgingsbrukerSistEndringRepository oppfolgingsbrukerSistEndringRepository,
            KafkaService kafkaService,
            LeaderElectionClient leaderElectionClient,
            UnleashService unleashService
    ) {
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.oppfolgingsbrukerSistEndringRepository = oppfolgingsbrukerSistEndringRepository;
        this.kafkaService = kafkaService;
        this.leaderElectionClient = leaderElectionClient;
        this.unleashService = unleashService;
    }

    @Scheduled(fixedDelay = TEN_SECONDS, initialDelay = TEN_SECONDS)
    public void publiserBrukereSomErEndretPaKafka() {
        if (leaderElectionClient.isLeader()) {
            if(unleashService.erSkruAvPubliseringPaKafkaEnabled()) {
                log.info("Publisering av brukere på kafka er skrudd av");
            } else {
                publisereArenaBrukerEndringer();
            }
        }
    }

    @Transactional
    void publisereArenaBrukerEndringer() {
        try {
            OppfolgingsbrukerSistEndretEntity sistEndret = oppfolgingsbrukerSistEndringRepository.hentSistEndret();
            List<OppfolgingsbrukerEntity> brukere = oppfolgingsbrukerRepository.changesSinceLastCheckSql(
                    sistEndret.getFodselsnr(), sistEndret.getOppfolgingsbrukerSistEndring()
            );

            if (!brukere.isEmpty()) {
                OppfolgingsbrukerEntity sisteBruker = brukere.get(brukere.size() - 1);
                oppfolgingsbrukerSistEndringRepository.updateLastcheck(sisteBruker.getFodselsnr(), sisteBruker.getTimestamp());
                log.info("Legger {} brukere til kafka", brukere.size());
                brukere.forEach(bruker -> {
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
