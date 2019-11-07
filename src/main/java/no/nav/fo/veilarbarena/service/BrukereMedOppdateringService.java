package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.scheduled.UserChangeListener;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.domain.PersonId.aktorId;

@Slf4j
public class BrukereMedOppdateringService implements UserChangeListener {

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;
    private final AktoerRegisterService aktoerRegisterService;

    @Inject
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
