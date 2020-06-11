package no.nav.veilarbarena.repository;

import no.nav.veilarbarena.domain.Oppfolgingsbruker;
import no.nav.veilarbarena.utils.LocalH2Database;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OppfolgingsbrukerRepositoryTest {

    @Before
    public void cleanup() {
        JdbcTemplate db = LocalH2Database.getDb();
        String insertBrukere = TestUtils.readTestResourceFile("oppfolgingsbrukere.sql");
        db.execute("DELETE FROM OPPFOLGINGSBRUKER");
        db.execute(insertBrukere);
    }

    @Test
    public void skal_hente_bruker() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        Optional<Oppfolgingsbruker> bruker = repository.hentOppfolgingsbruker("12345678900");

        assertTrue(bruker.isPresent());
    }

    @Test
    public void skal_hente_brukere() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<Oppfolgingsbruker> brukere = repository.hentOppfolgingsbrukere(List.of("12345678900", "12345678901"));

        assertEquals(2, brukere.size());
    }


    @Test
    public void skal_fungere_hvis_ingen_brukere() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<Oppfolgingsbruker> brukere = repository.hentOppfolgingsbrukere(Collections.emptyList());

        assertTrue(brukere.isEmpty());
    }

    @Test
    public void skal_hente_brukere_som_er_endret_og_har_riktig_status() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<Oppfolgingsbruker> brukere = repository.changesSinceLastCheckSql("12355", ZonedDateTime.now().minusDays(1));

        assertEquals(3, brukere.size());
    }

    @Test
    public void skal_hente_brukere_med_lik_timestamp_men_hoyere_fnr() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());
        ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS);

        insertBrukerMedTimestamp(Timestamp.from(tomorrow.toInstant()));

        List<Oppfolgingsbruker> brukere = repository.changesSinceLastCheckSql("12345678908", tomorrow);

        assertEquals(1, brukere.size());
        assertEquals("12345678909", brukere.get(0).getFodselsnr());
    }

    @Test
    public void skal_ikke_hente_brukere_som_ikke_er_endret() {
        OppfolgingsbrukerRepository repository = new OppfolgingsbrukerRepository(LocalH2Database.getDb());

        List<Oppfolgingsbruker> brukere = repository.changesSinceLastCheckSql("12355", ZonedDateTime.now().plusDays(1));

        assertTrue(brukere.isEmpty());
    }

    private void insertBrukerMedTimestamp(Timestamp timestamp) {
        LocalH2Database.getDb().update(format("INSERT INTO OPPFOLGINGSBRUKER (PERSON_ID, FODSELSNR, ETTERNAVN, FORNAVN, FORMIDLINGSGRUPPEKODE, KVALIFISERINGSGRUPPEKODE, RETTIGHETSGRUPPEKODE, ER_DOED, TIDSSTEMPEL) VALUES (123, '12345678909', 'Nordman', 'Knut', 'ARBS', 'BFORM', 'VLONN', 'N', '%s')", timestamp.toString()));
    }

}
