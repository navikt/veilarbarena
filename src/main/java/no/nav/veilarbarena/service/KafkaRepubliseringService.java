package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class KafkaRepubliseringService {

    private final static int OPPFOLGINGSPERIODE_PAGE_SIZE = 1000;

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    private final KafkaProducerService kafkaProducerService;

    @Autowired
    public KafkaRepubliseringService(
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            KafkaProducerService kafkaProducerService
    ) {
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void republiserEndringPaBrukere(int fromOffset) {
        log.info("Starter republisering fra offset {}", fromOffset);

        int currentOffset = fromOffset;

        while (true) {
            List<OppfolgingsbrukerEntity> brukere = oppfolgingsbrukerRepository.hentBrukerePage(currentOffset, OPPFOLGINGSPERIODE_PAGE_SIZE);

            if (brukere.isEmpty()) {
                break;
            }

            currentOffset += brukere.size();

            log.info("Republiserer endring på brukere. CurrentOffset={} BatchSize={}", currentOffset, brukere.size());

            brukere.forEach(this::republiserEndringPaBruker);
        }
    }

    private void republiserEndringPaBruker(OppfolgingsbrukerEntity oppfolgingsbrukerEntity) {
        var endringPaBrukerV2 = DtoMapper.tilEndringPaaOppfoelgingsBrukerV2(oppfolgingsbrukerEntity);

        kafkaProducerService.publiserEndringPaOppfolgingsbrukerV2Aiven(endringPaBrukerV2);
    }

}
