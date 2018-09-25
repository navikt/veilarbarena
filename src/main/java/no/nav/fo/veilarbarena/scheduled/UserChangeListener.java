package no.nav.fo.veilarbarena.scheduled;

import no.nav.fo.veilarbarena.domain.User;

@FunctionalInterface
public interface UserChangeListener {
    void userChanged(User user);
}
