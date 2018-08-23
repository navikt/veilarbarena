package no.nav.fo.veilarbarena.scheduled;

@FunctionalInterface
public interface UserChangeListener {
    void userChanged(User user);
}
