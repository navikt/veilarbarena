package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.scheduled.UserChangeListener;

import javax.inject.Inject;

public class BrukereMedOppdateringService implements UserChangeListener {

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;

    @Inject
    public BrukereMedOppdateringService(OppfolgingsbrukerEndringTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void userChanged(User user) {
        kafkaTemplate.send(user);
    }
}
