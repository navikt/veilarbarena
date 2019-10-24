package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.client.AktoerRegisterClient;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.scheduled.UserChangeListener;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.domain.PersonId.aktorId;

@Slf4j
public class BrukereMedOppdateringService implements UserChangeListener {

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;
    private final AktoerRegisterClient aktoerRegisterClient;

    @Inject
    public BrukereMedOppdateringService(OppfolgingsbrukerEndringTemplate kafkaTemplate, AktoerRegisterClient aktoerRegisterClient) {
        this.kafkaTemplate = kafkaTemplate;
        this.aktoerRegisterClient = aktoerRegisterClient;
    }

    @Override
    public void userChanged(User user) {
        final String aktorId = aktoerRegisterClient.tilAktorId(user.getFodselsnr().get());
        if (!aktorId.isEmpty()) {
            kafkaTemplate.send(user.withAktoerid(aktorId(aktorId)));
        } else {
            log.warn("Couldnt find aktorId for fnr: {}", user.getFodselsnr().get());
        }
    }
}
