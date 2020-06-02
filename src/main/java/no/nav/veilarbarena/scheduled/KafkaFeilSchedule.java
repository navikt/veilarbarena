package no.nav.veilarbarena.scheduled;

import no.nav.common.leaderelection.LeaderElectionClient;
import no.nav.veilarbarena.domain.FeiletKafkaBruker;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.repository.KafkaRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
    public void sendVedtakSendtFeiledeKafkaMeldinger() {
        if (leaderElectionClient.isLeader()) {
            List<String> feiledeBrukereFnr = kafkaRepository.hentFeiledeBrukere()
                    .stream()
                    .map(FeiletKafkaBruker::getFodselsnr)
                    .collect(Collectors.toList());

            List<OppfolgingsbrukerDTO> oppfolgingsbrukere = oppfolgingsbrukerRepository.hentOppfolgingsbrukere(feiledeBrukereFnr);
            // TODO: Send bruker
            oppfolgingsbrukere.forEach(b -> kafkaService.sendBrukerEndret(null));
        }
    }

}
