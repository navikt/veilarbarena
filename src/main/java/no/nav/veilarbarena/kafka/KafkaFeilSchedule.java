package no.nav.veilarbarena.kafka;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.domain.FeiletKafkaBruker;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.repository.KafkaRepository;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbvedtaksstotte.domain.FeiletKafkaMelding;
import no.nav.veilarbvedtaksstotte.domain.enums.KafkaTopic;
import no.nav.veilarbvedtaksstotte.repository.KafkaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KafkaFeilSchedule {

    private final static long SCHEDULE_DELAY = 5 * 60 * 1000; // 5 minutes

    private final KafkaRepository kafkaRepository;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    private final KafkaProducer kafkaProducer;

    @Autowired
    public KafkaFeilSchedule(
            KafkaRepository kafkaRepository,
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            KafkaProducer kafkaProducer
    ) {
        this.kafkaRepository = kafkaRepository;
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Scheduled(fixedDelay = SCHEDULE_DELAY, initialDelay = 60 * 1000)
    public void sendVedtakSendtFeiledeKafkaMeldinger() {
        List<String> feiledeBrukereFnr = kafkaRepository.hentFeiledeBrukere()
                .stream()
                .map(FeiletKafkaBruker::getFodselsnr)
                .collect(Collectors.toList());

        List<OppfolgingsbrukerDTO>

        feiledeBrukere.forEach(kafkaProducer::se);
    }

}
