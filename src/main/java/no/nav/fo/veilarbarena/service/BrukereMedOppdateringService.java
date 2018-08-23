package no.nav.fo.veilarbarena.service;

import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.scheduled.UserChangeListener;
import no.nav.fo.veilarbarena.scheduled.UserChangePublisher;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

public class BrukereMedOppdateringService implements UserChangeListener {

    private final OppfolgingsbrukerEndringTemplate kafkaTemplate;
    private final UserChangePublisher userChangePublisher;

    @Inject
    public BrukereMedOppdateringService(OppfolgingsbrukerEndringTemplate kafkaTemplate, UserChangePublisher userChangePublisher) {
        this.kafkaTemplate = kafkaTemplate;
        this.userChangePublisher = userChangePublisher;
    }

    @PostConstruct
    public void setup() {
        userChangePublisher.subscribe(this);
    }

    @Override
    public void userChanged(User user) {
        kafkaTemplate.send(user);
    }
}
