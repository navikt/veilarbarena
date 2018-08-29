package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.dialogarena.aktor.AktorService;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.scheduled.UserChangeListener;

import javax.inject.Inject;
import java.util.Optional;

import static no.nav.fo.veilarbarena.domain.PersonId.aktorId;

@Slf4j
public class BrukereMedOppdateringService implements UserChangeListener {

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;
    private final AktorService aktorService;

    @Inject
    public BrukereMedOppdateringService(OppfolgingsbrukerEndringTemplate kafkaTemplate, AktorService aktorService) {
        this.kafkaTemplate = kafkaTemplate;
        this.aktorService = aktorService;
    }

    @Override
    public void userChanged(User user) {
        Optional<String> aktorId = aktorService.getAktorId(user.getFodselsnr().get());
        if (aktorId.isPresent()) {
            kafkaTemplate.send(user.withAktoerid(aktorId(aktorId.get())));
        } else {
            log.warn("Couldnt find aktorId for fnr: {}", user.getFodselsnr().get());
        }
    }
}
