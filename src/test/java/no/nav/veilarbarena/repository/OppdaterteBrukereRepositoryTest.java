package no.nav.veilarbarena.repository;

import no.nav.veilarbarena.repository.entity.OppdatertBrukerEntity;
import no.nav.veilarbarena.utils.LocalH2Database;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OppdaterteBrukereRepositoryTest {

    @Before
    public void cleanup() {
        JdbcTemplate db = LocalH2Database.getDb();
        String insertBrukere = TestUtils.readTestResourceFile("oppfolgingsbrukere.sql");
        db.execute("DELETE FROM OPPFOLGINGSBRUKER");
        db.execute("DELETE FROM OPPDATERTE_BRUKERE");
        db.execute(insertBrukere);
    }

    @Test
    public void skal_inserte_alle_unike_brukere() {
        JdbcTemplate db = LocalH2Database.getDb();
        OppdaterteBrukereRepository oppdaterteBrukerRepository = new OppdaterteBrukereRepository(db);
        oppdaterteBrukerRepository.insertAlleBrukereFraOppfolgingsbrukerTabellen(Date.valueOf(LocalDate.now()));

        assertEquals(Long.valueOf(3), oppdaterteBrukerRepository.hentAntallBrukereSomSkalOppdaters());
    }

    @Test
    public void skal_slette_oppdatering() {
        JdbcTemplate db = LocalH2Database.getDb();
        OppdaterteBrukereRepository oppdaterteBrukerRepository = new OppdaterteBrukereRepository(db);
        oppdaterteBrukerRepository.insertOppdatering("123", Date.valueOf(LocalDate.now()));

        OppdatertBrukerEntity oppdatertBrukerEntity = oppdaterteBrukerRepository.hentBrukereMedEldsteEndringer().get(0);
        long antallBrukereForSletting = oppdaterteBrukerRepository.hentAntallBrukereSomSkalOppdaters();
        oppdaterteBrukerRepository.slettOppdatering(oppdatertBrukerEntity);
        long antallBrukereEtterSletting = oppdaterteBrukerRepository.hentAntallBrukereSomSkalOppdaters();

        assertEquals(1, antallBrukereForSletting);
        assertEquals(0, antallBrukereEtterSletting);
    }

    @Test
    public void skal_hente_eldste_oppdatering() {
        JdbcTemplate db = LocalH2Database.getDb();
        OppdaterteBrukereRepository oppdaterteBrukerRepository = new OppdaterteBrukereRepository(db);
        final String eldstFnr = "12345";
        final String nyFnr = "123";

        oppdaterteBrukerRepository.insertOppdatering(nyFnr, Date.valueOf(LocalDate.now()));
        oppdaterteBrukerRepository.insertOppdatering(eldstFnr, Date.valueOf(LocalDate.now().minusYears(1)));

        OppdatertBrukerEntity eldstBruker = oppdaterteBrukerRepository.hentBrukereMedEldsteEndringer().get(0);
        oppdaterteBrukerRepository.slettOppdatering(eldstBruker);
        OppdatertBrukerEntity nesteBruker = oppdaterteBrukerRepository.hentBrukereMedEldsteEndringer().get(0);
        oppdaterteBrukerRepository.slettOppdatering(nesteBruker);
        List<OppdatertBrukerEntity> forventetTom = oppdaterteBrukerRepository.hentBrukereMedEldsteEndringer();

        assertEquals(eldstFnr, eldstBruker.getFodselsnr());
        assertEquals(nyFnr, nesteBruker.getFodselsnr());
        assertTrue(forventetTom.isEmpty());
    }

    @Test
    public void skal_hente_liste_av_oppdateringer() {
        JdbcTemplate db = LocalH2Database.getDb();
        OppdaterteBrukereRepository oppdaterteBrukerRepository = new OppdaterteBrukereRepository(db);
        oppdaterteBrukerRepository.insertAlleBrukereFraOppfolgingsbrukerTabellen(Date.valueOf(LocalDate.now()));
        List<OppdatertBrukerEntity> brukereSomSkalOppdateres = oppdaterteBrukerRepository.hentBrukereMedEldsteEndringer();
        assertEquals(brukereSomSkalOppdateres.size(), 3);
    }
}
