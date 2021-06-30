package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerSistEndringRepository;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerSistEndretEntity;
import no.nav.veilarbarena.service.KafkaProducerService;
import no.nav.veilarbarena.service.UnleashService;
import no.nav.veilarbarena.utils.DtoMapper;
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

    private final LeaderElectionClient leaderElectionClient;

    private final UnleashService unleashService;

    private final KafkaProducerService kafkaProducerService;

    private final AktorOppslagClient aktorOppslagClient;

    @Autowired
    public OppfolgingsbrukerEndretSchedule(
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            OppfolgingsbrukerSistEndringRepository oppfolgingsbrukerSistEndringRepository,
            LeaderElectionClient leaderElectionClient,
            UnleashService unleashService,
            KafkaProducerService kafkaProducerService,
            AktorOppslagClient aktorOppslagClient
    ) {
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.oppfolgingsbrukerSistEndringRepository = oppfolgingsbrukerSistEndringRepository;
        this.leaderElectionClient = leaderElectionClient;
        this.unleashService = unleashService;
        this.kafkaProducerService = kafkaProducerService;
        this.aktorOppslagClient = aktorOppslagClient;
    }

    @Scheduled(fixedDelay = TEN_SECONDS, initialDelay = TEN_SECONDS)
    public void publiserBrukereSomErEndretPaKafka() {
        if (leaderElectionClient.isLeader()) {
            if (unleashService.erSkruAvPubliseringPaKafkaEnabled()) {
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

            if (brukere.isEmpty()) {
                log.info("Ingen nye endringer å publisere på kafka");
                return;
            }

            log.info("Legger {} brukere til kafka", brukere.size());

            OppfolgingsbrukerEntity sisteBruker = brukere.get(brukere.size() - 1);
            oppfolgingsbrukerSistEndringRepository.updateLastcheck(sisteBruker.getFodselsnr(), sisteBruker.getTimestamp());

            brukere.forEach(bruker -> {
                var endringPaBruker = DtoMapper.tilEndringPaaOppfoelgingsBrukerV1(bruker);
                AktorId aktorId = aktorOppslagClient.hentAktorId(Fnr.of(endringPaBruker.getFodselsnr()));

                if (aktorId == null) {
                    throw new IllegalStateException("Fant ikke aktørid for en bruker, får ikke sendt til kafka");
                }

                endringPaBruker.setAktoerid(aktorId.get());

                kafkaProducerService.publiserEndringPaOppfolgingsbruker(endringPaBruker);
            });
        } catch(Exception e) {
            log.error("Feil ved publisering av arena endringer til kafka", e);
        }
    }

}
