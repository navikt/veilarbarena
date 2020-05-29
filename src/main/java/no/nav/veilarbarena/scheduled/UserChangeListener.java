package no.nav.veilarbarena.scheduled;

import no.nav.veilarbarena.domain.User;

@FunctionalInterface
public interface UserChangeListener {
    void userChanged(User user);
}
