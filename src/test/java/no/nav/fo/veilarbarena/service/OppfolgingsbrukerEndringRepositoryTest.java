package no.nav.fo.veilarbarena.service;

import io.vavr.collection.List;
import no.nav.fo.veilarbarena.DbTest;
import no.nav.fo.veilarbarena.domain.FeiletKafkaRecord;
import no.nav.fo.veilarbarena.domain.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.Utils.lagNyBruker;
import static org.assertj.core.api.Assertions.assertThat;

class OppfolgingsbrukerEndringRepositoryTest extends DbTest {

    @Inject
    private OppfolgingsbrukerEndringRepository oppfolgingsbrukerEndringRepository;

    @BeforeAll
    static void setup() {
        initSpringContext(OppfolgingsbrukerEndringRepository.class);
    }

    @Test
    void insertFeiletBruker() {
        User user = lagNyBruker();
        oppfolgingsbrukerEndringRepository.insertFeiletBruker(user);

        final List<FeiletKafkaRecord> feiletKafkaRecord = oppfolgingsbrukerEndringRepository.hentFeiledeBrukere();
        assertThat(feiletKafkaRecord.size()).isEqualTo(1);
        assertThat(feiletKafkaRecord.get(0).getFodselsnr().value).isEqualTo(user.getFodselsnr().get());
    }

    @Test
    void deleteFeiletBruker() {
        User user = lagNyBruker();
        oppfolgingsbrukerEndringRepository.insertFeiletBruker(user);
        List<FeiletKafkaRecord> feiletKafkaRecord = oppfolgingsbrukerEndringRepository.hentFeiledeBrukere();

        assertThat(feiletKafkaRecord.size()).isEqualTo(1);
        oppfolgingsbrukerEndringRepository.deleteFeiletBruker(user);
        feiletKafkaRecord = oppfolgingsbrukerEndringRepository.hentFeiledeBrukere();

        assertThat(feiletKafkaRecord.size()).isEqualTo(0);
    }
}