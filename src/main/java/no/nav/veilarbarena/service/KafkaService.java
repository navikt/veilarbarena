package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerEndretDTO;
import no.nav.veilarbarena.kafka.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {

    private final KafkaProducer kafkaProducer;

    private final AktorregisterClient aktorregisterClient;

    @Autowired
    public KafkaService(KafkaProducer kafkaProducer, AktorregisterClient aktorregisterClient) {
        this.kafkaProducer = kafkaProducer;
        this.aktorregisterClient = aktorregisterClient;
    }

    public void sendBrukerEndret(OppfolgingsbrukerEndretDTO bruker) {
        oppdaterMedAktorId(bruker);
        kafkaProducer.sendEndringPaOppfolgingsbruker(bruker, false);
    }

    public void sendTidligereFeiletBrukerEndret(OppfolgingsbrukerEndretDTO bruker) {
        oppdaterMedAktorId(bruker);
        kafkaProducer.sendEndringPaOppfolgingsbruker(bruker, true);
    }

    private void oppdaterMedAktorId(OppfolgingsbrukerEndretDTO oppfolgingsbrukerEndretDTO) {
        if (oppfolgingsbrukerEndretDTO.getAktoerid() == null) {
            final AktorId aktorId = aktorregisterClient.hentAktorId(Fnr.of(oppfolgingsbrukerEndretDTO.getFodselsnr()));

            if (aktorId == null) {
                throw new IllegalStateException("Fant ikke aktørid for en bruker, får ikke sendt til kafka");
            }

            oppfolgingsbrukerEndretDTO.setAktoerid(aktorId.get());
        }
    }

}
