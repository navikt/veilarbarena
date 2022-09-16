package no.nav.veilarbarena.repository;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.utils.LocalH2Database;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.lang.String.format;
import static org.junit.Assert.*;

public class OppfolgingsbrukerRepositoryTest {

    @Before
    public void cleanup() {
        JdbcTemplate db = LocalH2Database.getDb();
        String insertBrukere = TestUtils.readTestResourceFile("oppfolgingsbrukere.sql");
        db.execute("DELETE FROM OPPFOLGINGSBRUKER");
        db.execute(insertBrukere);
    }

    @Test
    public void hentBrukerePage__skal_hente_page_med_brukere() {
        JdbcTemplate db = LocalH2Database.getDb();
        db.execute("DELETE FROM OPPFOLGINGSBRUKER");

        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(db);

        Fnr fnr1 = Fnr.of("fnr1");
        Fnr fnr2 = Fnr.of("fnr2");
        Fnr fnr3 = Fnr.of("fnr3");

        insertBruker(fnr2);
        insertBruker(fnr1);
        insertBruker(fnr3);

        List<OppfolgingsbrukerEntity> brukerePage1 = repository.hentBrukerePage(0, 1);
        assertEquals(1, brukerePage1.size());
        assertEquals(fnr1.get(), brukerePage1.get(0).getFodselsnr());

        List<OppfolgingsbrukerEntity> brukerePage2 = repository.hentBrukerePage(1, 2);
        assertEquals(2, brukerePage2.size());
        assertEquals(fnr2.get(), brukerePage2.get(0).getFodselsnr());
        assertEquals(fnr3.get(), brukerePage2.get(1).getFodselsnr());
    }

    @Test
    public void skal_hente_bruker() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        Optional<OppfolgingsbrukerEntity> bruker = repository.hentOppfolgingsbruker("12345678900");

        assertTrue(bruker.isPresent());
    }

    @Test
    public void skal_hente_brukers_personId() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        Optional<String> personId = repository.hentOppfolgingsbrukerSinPersonId("12345678900");

        assertTrue(personId.isPresent());
        assertEquals("1", personId.get());
    }

    @Test
    public void skal_hente_brukere() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<OppfolgingsbrukerEntity> brukere = repository.hentOppfolgingsbrukere(List.of("12345678900", "12345678901"));

        assertEquals(2, brukere.size());
    }


    @Test
    public void skal_fungere_hvis_ingen_brukere() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<OppfolgingsbrukerEntity> brukere = repository.hentOppfolgingsbrukere(Collections.emptyList());

        assertTrue(brukere.isEmpty());
    }

    @Test
    public void skal_hente_brukere_som_er_endret() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<OppfolgingsbrukerEntity> brukere = repository.changesSinceLastCheckSql("12355", ZonedDateTime.now().minusDays(1));

        assertEquals(4, brukere.size());
    }

    @Test
    public void skal_hente_brukere_med_lik_timestamp_men_hoyere_fnr() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());
        ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);

        insertBruker(Fnr.of("12345678909"), Timestamp.from(tomorrow.toInstant()));

        List<OppfolgingsbrukerEntity> brukere = repository.changesSinceLastCheckSql("12345678908", tomorrow);

        assertEquals(1, brukere.size());
        assertEquals("12345678909", brukere.get(0).getFodselsnr());
    }

    @Test
    public void skal_ikke_hente_brukere_som_ikke_er_endret() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<OppfolgingsbrukerEntity> brukere = repository.changesSinceLastCheckSql("12355", ZonedDateTime.now().plusDays(1));

        assertTrue(brukere.isEmpty());
    }

    private void insertBruker(Fnr fnr) {
        insertBruker(fnr, Timestamp.from(Instant.now()));
    }

    private void insertBruker(Fnr fnr, Timestamp timestamp) {
        int personId = new Random().nextInt();
        LocalH2Database.getDb().update(format("INSERT INTO OPPFOLGINGSBRUKER (PERSON_ID, FODSELSNR, ETTERNAVN, FORNAVN, FORMIDLINGSGRUPPEKODE, KVALIFISERINGSGRUPPEKODE, RETTIGHETSGRUPPEKODE, ER_DOED, TIDSSTEMPEL) VALUES (%d, '%s', 'Nordman', 'Knut', 'ARBS', 'BFORM', 'VLONN', 'N', '%s')", personId, fnr, timestamp.toString()));
    }

}
