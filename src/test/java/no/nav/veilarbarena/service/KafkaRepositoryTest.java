package no.nav.veilarbarena.service;

import io.vavr.collection.List;
import no.nav.veilarbarena.DbTest;
import no.nav.veilarbarena.domain.FeiletKafkaRecord;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.repository.KafkaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static no.nav.veilarbarena.Utils.lagNyBruker;
import static org.assertj.core.api.Assertions.assertThat;

class KafkaRepositoryTest extends DbTest {

    @Inject
    private KafkaRepository kafkaRepository;

    @BeforeAll
    static void setup() {
        initSpringContext(KafkaRepository.class);
    }

    @Test
    void insertFeiletBruker() {
        User user = lagNyBruker();
        kafkaRepository.insertFeiletBruker(user);

        final List<FeiletKafkaRecord> feiletKafkaRecord = kafkaRepository.hentFeiledeBrukere();
        assertThat(feiletKafkaRecord.size()).isEqualTo(1);
        assertThat(feiletKafkaRecord.get(0).getFodselsnr().value).isEqualTo(user.getFodselsnr().get());
    }

    @Test
    void deleteFeiletBruker() {
        User user = lagNyBruker();
        kafkaRepository.insertFeiletBruker(user);
        List<FeiletKafkaRecord> feiletKafkaRecord = kafkaRepository.hentFeiledeBrukere();

        assertThat(feiletKafkaRecord.size()).isEqualTo(1);
        kafkaRepository.deleteFeiletBruker(user);
        feiletKafkaRecord = kafkaRepository.hentFeiledeBrukere();

        assertThat(feiletKafkaRecord.size()).isEqualTo(0);
    }
}