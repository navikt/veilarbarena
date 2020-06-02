package no.nav.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.kafka.OppfolgingsbrukerEndringTemplate;
import no.nav.veilarbarena.scheduled.UserChangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import static no.nav.veilarbarena.domain.PersonId.aktorId;

@Slf4j
public class BrukereMedOppdateringService implements UserChangeListener {

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;
    private final AktoerRegisterService aktoerRegisterService;

    @Autowired
    public BrukereMedOppdateringService(OppfolgingsbrukerEndringTemplate kafkaTemplate, AktoerRegisterService aktoerRegisterService) {
        this.kafkaTemplate = kafkaTemplate;
        this.aktoerRegisterService = aktoerRegisterService;
    }

    @Override
    public void userChanged(User user) {
        if (user != null && user.getAktoerid() == null) {
            final String aktorId = aktoerRegisterService.tilAktorId(user.getFodselsnr().get());
            if (aktorId != null) {
                kafkaTemplate.send(user.withAktoerid(aktorId(aktorId)));
            } else {
                log.warn("Fant ikke aktørid for en bruker, får ikke sendt til kafka");
            }
        } else {
            kafkaTemplate.send(user);
        }
    }
}
