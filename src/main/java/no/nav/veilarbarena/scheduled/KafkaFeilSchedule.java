package no.nav.veilarbarena.scheduled;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.job.leader_election.LeaderElectionClient;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerEndretDTO;
import no.nav.veilarbarena.repository.KafkaRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.entity.FeiletKafkaBrukerEntity;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Component
public class KafkaFeilSchedule {

    private final static long FIVE_MINUTES = 5 * 60 * 1000;

    private final static long ONE_MINUTE = 60 * 1000;

    private final LeaderElectionClient leaderElectionClient;

    private final KafkaRepository kafkaRepository;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    private final KafkaService kafkaService;

    @Autowired
    public KafkaFeilSchedule(
            LeaderElectionClient leaderElectionClient,
            KafkaRepository kafkaRepository,
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            KafkaService kafkaService) {
        this.leaderElectionClient = leaderElectionClient;
        this.kafkaRepository = kafkaRepository;
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.kafkaService = kafkaService;
    }

    @Scheduled(fixedDelay = FIVE_MINUTES, initialDelay = ONE_MINUTE)
    public void publiserTidligereFeiletBrukerePaKafka() {
        if (leaderElectionClient.isLeader()) {
            List<String> feiledeBrukereFnr = kafkaRepository.hentFeiledeBrukere()
                    .stream()
                    .map(FeiletKafkaBrukerEntity::getFodselsnr)
                    .collect(Collectors.toList());

            if (feiledeBrukereFnr.isEmpty()) {
                return;
            }

            List<OppfolgingsbrukerEntity> oppfolgingsbrukere = oppfolgingsbrukerRepository.hentOppfolgingsbrukere(feiledeBrukereFnr);

            log.info(format(
                    "Publiser tidligere feilede brukere på kafka. Feilede brukere: %d Brukere fra database: %d",
                    feiledeBrukereFnr.size(), oppfolgingsbrukere.size())
            );

            oppfolgingsbrukere.forEach(bruker ->
                    kafkaService.sendTidligereFeiletBrukerEndret(OppfolgingsbrukerEndretDTO.fraOppfolgingsbruker(bruker))
            );
        }
    }

}
