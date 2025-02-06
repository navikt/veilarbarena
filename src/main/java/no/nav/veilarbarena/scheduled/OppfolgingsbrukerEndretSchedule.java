package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.veilarbarena.client.unleash.VeilarbaktivitetUnleashClient;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.entity.OppdatertBrukerEntity;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.service.KafkaProducerService;
import no.nav.veilarbarena.service.PubliserOppfolgingsbrukerService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

import static no.nav.common.utils.EnvironmentUtils.isProduction;

@Slf4j
@Component
public class OppfolgingsbrukerEndretSchedule {

    private final static long TEN_SECONDS = 10 * 1000;
    private final OppdaterteBrukereRepository oppdaterteBrukereRepository;
    private final LeaderElectionClient leaderElectionClient;
    private final VeilarbaktivitetUnleashClient veilarbaktivitetUnleashClient;
    private final PubliserOppfolgingsbrukerService publiserOppfolgingsbrukerService;

    @Autowired
    public OppfolgingsbrukerEndretSchedule(
            OppdaterteBrukereRepository oppdaterteBrukereRepository,
            LeaderElectionClient leaderElectionClient,
            VeilarbaktivitetUnleashClient veilarbaktivitetUnleashClient,
            PubliserOppfolgingsbrukerService publiserOppfolgingsbrukerService
    ) {
        this.oppdaterteBrukereRepository = oppdaterteBrukereRepository;
        this.leaderElectionClient = leaderElectionClient;
        this.veilarbaktivitetUnleashClient = veilarbaktivitetUnleashClient;
        this.publiserOppfolgingsbrukerService = publiserOppfolgingsbrukerService;
    }

    @Scheduled(fixedDelay = TEN_SECONDS, initialDelay = TEN_SECONDS)
    public void publiserBrukereSomErEndretPaKafkaV2() {
        if (leaderElectionClient.isLeader() && !veilarbaktivitetUnleashClient.oppfolgingsbrukerBatchIsDisabled().orElse(false)) {
            publisereArenaBrukerEndringerV2();
        }
    }

    void publisereArenaBrukerEndringerV2() {
        log.info("Skal sende {} bruker oppdateringer til kafka", oppdaterteBrukereRepository.hentAntallBrukereSomSkalOppdaters());
        /* Only check feature toggle every 1000 processed messages */
        int prosessedSinceLaseUnleashCheck = 0;
        while (true) {
            if (prosessedSinceLaseUnleashCheck > 1000) {
                prosessedSinceLaseUnleashCheck = 0;
                if (veilarbaktivitetUnleashClient.oppfolgingsbrukerBatchIsDisabled().orElse(false)) {
                    break;
                }
            }

            List<OppdatertBrukerEntity> brukerOppdateringer = oppdaterteBrukereRepository.hentBrukereMedEldsteEndringer();
            if (brukerOppdateringer == null || brukerOppdateringer.isEmpty()) {
                return;
            }
            brukerOppdateringer.forEach(brukerOppdatering -> {
                LocalDate foreldetMeldingPgaDataWipe = LocalDate.now().minusMonths(1);
                if (isProduction().orElse(false) || brukerOppdatering.getTidsstempel().toLocalDate().isAfter(foreldetMeldingPgaDataWipe)) {
                    publiserOppfolgingsbrukerService.publiserOppfolgingsbruker(brukerOppdatering.getFodselsnr());
                } else {
                    log.info("Ignorerer rader som har et tidsstempel i som er eldre enn 1 måned");
                }
                oppdaterteBrukereRepository.slettOppdatering(brukerOppdatering);
            });
            prosessedSinceLaseUnleashCheck += 10;
        }
    }
}
