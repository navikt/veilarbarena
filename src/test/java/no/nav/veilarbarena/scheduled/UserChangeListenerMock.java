package no.nav.veilarbarena.scheduled;


import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.domain.User;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserChangeListenerMock implements UserChangeListener {

    public UserChangeListenerMock() {
        System.out.println("!");
    }

    @Override
    public void userChanged(User user) {
        log.info(user.toString());
    }

}
