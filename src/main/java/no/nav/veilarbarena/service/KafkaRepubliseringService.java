package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
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

    private final AktorOppslagClient aktorOppslagClient;

    @Autowired
    public KafkaRepubliseringService(
            OppfolgingsbrukerRepository oppfolgingsbrukerRepository,
            KafkaProducerService kafkaProducerService,
            AktorOppslagClient aktorOppslagClient
    ) {
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.aktorOppslagClient = aktorOppslagClient;
    }

    public void republiserEndringPaBrukere() {
        int currentOffset = 0;

        while (true) {
            List<Fnr> unikeFnr = oppfolgingsbrukerRepository.hentUnikeBrukerePage(currentOffset, OPPFOLGINGSPERIODE_PAGE_SIZE);

            if (unikeFnr.isEmpty()) {
                break;
            }

            currentOffset += unikeFnr.size();

            log.info("Republiserer endring på brukere. CurrentOffset={} BatchSize={}", currentOffset, unikeFnr.size());

            unikeFnr.forEach(this::republiserEndringPaBruker);
        }
    }

    private void republiserEndringPaBruker(Fnr fnr) {
        var oppfolgingsbrukerEntity = oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get()).orElseThrow();
        var endringPaBrukerV2 = DtoMapper.tilEndringPaaOppfoelgingsBrukerV2(oppfolgingsbrukerEntity);

        kafkaProducerService.publiserEndringPaOppfolgingsbrukerV2Aiven(endringPaBrukerV2);
    }

}
