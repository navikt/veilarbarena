package no.nav.veilarbarena.repository;

import no.nav.veilarbarena.repository.entity.FeiletKafkaBrukerEntity;
import no.nav.veilarbarena.utils.LocalH2Database;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KafkaRepositoryTest {

    @Test
    public void skal_inserte_og_hente_feilet_bruker() {
        KafkaRepository kafkaRepository = new KafkaRepository(LocalH2Database.getDb());

        kafkaRepository.insertFeiletBruker("test-fnr");

        List<FeiletKafkaBrukerEntity> brukere = kafkaRepository.hentFeiledeBrukere();

        assertEquals(1, brukere.size());
        assertEquals("test-fnr", brukere.get(0).getFodselsnr());
    }

    @Test
    public void skal_slette_feilet_bruker() {
        KafkaRepository kafkaRepository = new KafkaRepository(LocalH2Database.getDb());

        kafkaRepository.insertFeiletBruker("test-fnr");
        kafkaRepository.deleteFeiletBruker("test-fnr");

        List<FeiletKafkaBrukerEntity> brukere = kafkaRepository.hentFeiledeBrukere();

        assertTrue(brukere.isEmpty());
    }

}
