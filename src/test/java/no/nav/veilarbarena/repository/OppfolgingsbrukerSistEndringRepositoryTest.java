package no.nav.veilarbarena.repository;

import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerSistEndretEntity;
import no.nav.veilarbarena.utils.LocalH2Database;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;


public class OppfolgingsbrukerSistEndringRepositoryTest {

    @Test
    public void skal_oppdatere_og_hente_siste_sjekk() {
        OppfolgingsbrukerSistEndringRepository repository = new OppfolgingsbrukerSistEndringRepository(LocalH2Database.getDb());

        ZonedDateTime now = ZonedDateTime.now();

        repository.updateLastcheck("test-fnr", now);

        OppfolgingsbrukerSistEndretEntity sistEndret = repository.hentSistEndret();

        assertEquals("test-fnr", sistEndret.getFodselsnr());
        assertEquals(now, sistEndret.getOppfolgingsbrukerSistEndring());
    }


}
