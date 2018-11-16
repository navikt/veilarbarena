package no.nav.fo.veilarbarena.scheduled;

import io.vavr.collection.List;
import no.nav.dialogarena.aktor.AktorServiceImpl;
import no.nav.fo.veilarbarena.DbTest;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.service.AktorServiceMock;
import no.nav.fo.veilarbarena.service.OppfolgingsbrukerEndringRepository;
import no.nav.sbl.sql.SqlUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.Utils.lagNyBruker;
import static org.assertj.core.api.Java6Assertions.assertThat;


class UserChangePublisherTest extends DbTest {

    @Inject
    private OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository;
    @Inject
    private UserChangePublisher userChangePublisher;
    @Inject
    private JdbcTemplate db;

    @BeforeAll
    static void setup() {
        initSpringContext(
                OppfolgingsbrukerEndringRepository.class,
                UserChangePublisher.class,
                UserChangeListenerMock.class,
                AktorServiceMock.class
        );
    }

    @Test
    void findChangesSinceLastCheck() {
        final User user = lagNyBruker();
        oppfolgingsbrukerEndringRepository.insertFeiletBruker(user);
        insertOppfolgingsBruker(user);

        final List<User> allFailedKafkaUsers = userChangePublisher.findAllFailedKafkaUsers();

        assertThat(allFailedKafkaUsers.get(0).getFodselsnr().get()).isEqualTo(user.getFodselsnr().get());

    }

    private void insertOppfolgingsBruker(User user) {
        SqlUtils.insert(db, "OPPFOLGINGSBRUKER")
                .value("FODSELSNR", user.getFodselsnr().get())
                .value("PERSON_ID", "123")
                .value("ETTERNAVN", "WAYNE")
                .value("FORNAVN", "BRUCE")
                .value("FORMIDLINGSGRUPPEKODE", "ISERV")
                .value("KVALIFISERINGSGRUPPEKODE", "ASDF")
                .value("RETTIGHETSGRUPPEKODE", "VLONN")
                .value("ER_DOED", "N")
                .execute();
    }
}