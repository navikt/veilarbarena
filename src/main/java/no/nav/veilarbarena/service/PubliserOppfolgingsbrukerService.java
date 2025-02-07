package no.nav.veilarbarena.service;

import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.stereotype.Service;

@Service
public class PubliserOppfolgingsbrukerService {
    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;
    private final KafkaProducerService kafkaProducerService;

    public PubliserOppfolgingsbrukerService(
        OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
        KafkaProducerService kafkaProducerService
    ) {
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public Boolean publiserOppfolgingsbruker(String fnr) {
        var bruker = oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr);
        if (bruker.isPresent()) {
            publiserPaKafka(bruker.get());
            return true;
        } else {
            return false;
        }
    }

    private void publiserPaKafka(OppfolgingsbrukerEntity bruker) {
        var endringPaBrukerV2 = DtoMapper.tilEndringPaaOppfoelgingsBrukerV2(bruker);
        kafkaProducerService.publiserEndringPaOppfolgingsbrukerV2(endringPaBrukerV2);
    }
}
