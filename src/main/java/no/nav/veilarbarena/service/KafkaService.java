package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.veilarbarena.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.veilarbarena.domain.PersonId.aktorId;

@Slf4j
@Service
public class KafkaService {

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;
    private final AktorregisterClient aktorregisterClient;

    @Autowired
    public KafkaService(OppfolgingsbrukerEndringTemplate kafkaTemplate, AktorregisterClient aktorregisterClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.aktorregisterClient = aktorregisterClient;
    }

    public void sendBrukerEndret(User user) {
        if (user.getAktoerid() == null) {
            final String aktorId = aktorregisterClient.hentAktorId(user.getFodselsnr().get());
            if (aktorId != null) {
                kafkaTemplate.send(user.withAktoerid(aktorId(aktorId)));
            } else {
                log.error("Fant ikke aktørid for en bruker, får ikke sendt til kafka");
            }
        } else {
            kafkaTemplate.send(user);
        }
    }
}
