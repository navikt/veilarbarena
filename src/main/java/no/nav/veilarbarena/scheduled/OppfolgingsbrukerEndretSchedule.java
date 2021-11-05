package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerSistEndringRepository;
import no.nav.veilarbarena.repository.entity.OppdatertBrukerEntity;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerSistEndretEntity;
import no.nav.veilarbarena.service.KafkaProducerService;
import no.nav.veilarbarena.service.MetricsService;
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

    private final OppdaterteBrukereRepository oppdaterteBrukereRepository;

    private final LeaderElectionClient leaderElectionClient;

    private final UnleashService unleashService;

    private final KafkaProducerService kafkaProducerService;

    private final AktorOppslagClient aktorOppslagClient;

    private final MetricsService metricsService;

    @Autowired
    public OppfolgingsbrukerEndretSchedule(
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            OppfolgingsbrukerSistEndringRepository oppfolgingsbrukerSistEndringRepository,
            OppdaterteBrukereRepository oppdaterteBrukereRepository, LeaderElectionClient leaderElectionClient,
            UnleashService unleashService,
            KafkaProducerService kafkaProducerService,
            AktorOppslagClient aktorOppslagClient,
            MetricsService metricsService
    ) {
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.oppfolgingsbrukerSistEndringRepository = oppfolgingsbrukerSistEndringRepository;
        this.oppdaterteBrukereRepository = oppdaterteBrukereRepository;
        this.leaderElectionClient = leaderElectionClient;
        this.unleashService = unleashService;
        this.kafkaProducerService = kafkaProducerService;
        this.aktorOppslagClient = aktorOppslagClient;
        this.metricsService = metricsService;
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

    @Scheduled(fixedDelay = TEN_SECONDS, initialDelay = TEN_SECONDS)
    public void publiserBrukereSomErEndretPaKafkaV2() {
        if (leaderElectionClient.isLeader()) {
            if (unleashService.erSkruAvPubliseringPaKafkaEnabled()) {
                log.info("Publisering av brukere på kafka er skrudd av");
            } else {
                publisereArenaBrukerEndringerV2();
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
                AktorId aktorId = aktorOppslagClient.hentAktorId(Fnr.of(bruker.getFodselsnr()));

                if (aktorId == null) {
                    throw new IllegalStateException("Fant ikke aktørid for en bruker, får ikke sendt til kafka");
                }

                var endringPaBrukerV1 = DtoMapper.tilEndringPaaOppfoelgingsBrukerV1(bruker, aktorId);

                kafkaProducerService.publiserEndringPaOppfolgingsbrukerV1OnPrem(endringPaBrukerV1);
            });
        } catch(Exception e) {
            log.error("Feil ved publisering av arena endringer til kafka", e);
        }
    }

    void publisereArenaBrukerEndringerV2() {
        log.info("Skal sende {} bruker oppdateringer til kafka", oppdaterteBrukereRepository.hentAntallBrukereSomSkalOppdaters());
        while (true) {
            OppdatertBrukerEntity brukerOppdatering = oppdaterteBrukereRepository.hentBrukerMedEldstEndring();
            if (brukerOppdatering == null) {
                return;
            }
            oppfolgingsbrukerRepository.hentOppfolgingsbruker(brukerOppdatering.getFodselsnr()).ifPresent(this::publiserPaKafka);
            oppdaterteBrukereRepository.slettOppdatering(brukerOppdatering);
        }
    }

    private void publiserPaKafka(OppfolgingsbrukerEntity bruker) {
        var endringPaBrukerV2 = DtoMapper.tilEndringPaaOppfoelgingsBrukerV2(bruker);
        kafkaProducerService.publiserEndringPaOppfolgingsbrukerV2Aiven(endringPaBrukerV2);
    }

}
