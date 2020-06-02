package no.nav.veilarbarena.service;

import no.nav.veilarbarena.DbTest;
import no.nav.veilarbarena.domain.FeiletKafkaBruker;
import no.nav.veilarbarena.repository.KafkaRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static no.nav.veilarbarena.Utils.lagNyBruker;
import static org.assertj.core.api.Assertions.assertThat;

class KafkaRepositoryTest extends DbTest {

//    @Inject
//    private KafkaRepository kafkaRepository;
//
//    @BeforeAll
//    static void setup() {
//        initSpringContext(KafkaRepository.class);
//    }
//
//    @Test
//    void insertFeiletBruker() {
//        User user = lagNyBruker();
//        kafkaRepository.insertFeiletBruker(user);
//
//        final List<FeiletKafkaBruker> feiletKafkaRecord = kafkaRepository.hentFeiledeBrukere();
//        assertThat(feiletKafkaRecord.size()).isEqualTo(1);
//        assertThat(feiletKafkaRecord.get(0).getFodselsnr().value).isEqualTo(user.getFodselsnr().get());
//    }
//
//    @Test
//    void deleteFeiletBruker() {
//        User user = lagNyBruker();
//        kafkaRepository.insertFeiletBruker(user);
//        List<FeiletKafkaBruker> feiletKafkaRecord = kafkaRepository.hentFeiledeBrukere();
//
//        assertThat(feiletKafkaRecord.size()).isEqualTo(1);
//        kafkaRepository.deleteFeiletBruker(user);
//        feiletKafkaRecord = kafkaRepository.hentFeiledeBrukere();
//
//        assertThat(feiletKafkaRecord.size()).isEqualTo(0);
//    }
}