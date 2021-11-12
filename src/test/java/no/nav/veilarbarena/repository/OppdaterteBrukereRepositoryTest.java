package no.nav.veilarbarena.repository;

import no.nav.veilarbarena.repository.entity.OppdatertBrukerEntity;
import no.nav.veilarbarena.utils.LocalH2Database;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OppdaterteBrukereRepositoryTest {
    private static JdbcTemplate db;
    private static OppdaterteBrukereRepository oppdaterteBrukerRepository;

    @BeforeClass
    public static void setupOnce() {
        db = LocalH2Database.getDb();
        oppdaterteBrukerRepository = new OppdaterteBrukereRepository(db);
    }

    @Before
    public void setup() {
        String insertBrukere = TestUtils.readTestResourceFile("oppfolgingsbrukere.sql");
        deleteFromOppfolgingsbruker();
        db.execute("DELETE FROM OPPDATERTE_BRUKERE");
        db.execute(insertBrukere);
    }

    @Test
    public void skal_inserte_alle_unike_brukere() {
        oppdaterteBrukerRepository.insertAlleBrukereFraOppfolgingsbrukerTabellen(Date.valueOf(LocalDate.now()));

        assertEquals(Long.valueOf(3), oppdaterteBrukerRepository.hentAntallBrukereSomSkalOppdaters());
    }

    @Test
    public void skal_slette_oppdatering() {
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
        oppdaterteBrukerRepository.insertAlleBrukereFraOppfolgingsbrukerTabellen(Date.valueOf(LocalDate.now()));
        List<OppdatertBrukerEntity> brukereSomSkalOppdateres = oppdaterteBrukerRepository.hentBrukereMedEldsteEndringer();
        assertEquals(brukereSomSkalOppdateres.size(), 3);
    }


    @Test
    public void setter_inn_brukere_som_er_oppdatert_etter_valgt_fra_dato() {
        deleteFromOppfolgingsbruker();

        LocalDate fraDato = LocalDate.of(2021, 7, 15);

        insertOppfolgingsbruker(1, "1", LocalDate.of(2021, 2, 13));
        insertOppfolgingsbruker(2, "2", LocalDate.of(2021, 10, 7));
        insertOppfolgingsbruker(3, "3", LocalDate.of(2021, 9, 17));
        insertOppfolgingsbruker(4, "4", LocalDate.of(2020, 11, 19));
        insertOppfolgingsbruker(5, "5", LocalDate.of(2021, 7, 29));

        oppdaterteBrukerRepository.insertBrukereFraOppfolgingsbrukerFraDato(Date.valueOf(LocalDate.now()), fraDato);

        List<OppdatertBrukerEntity> brukereSomSkalOppdateres = oppdaterteBrukerRepository.hentBrukereMedEldsteEndringer();

        assertThat(brukereSomSkalOppdateres, hasSize(3));
        assertThat(brukereSomSkalOppdateres.stream().map(OppdatertBrukerEntity::getFodselsnr).collect(Collectors.toList()),
                containsInAnyOrder("2", "5", "3"));
    }

    private void deleteFromOppfolgingsbruker() {
        db.execute("DELETE FROM OPPFOLGINGSBRUKER");
    }

    private void insertOppfolgingsbruker(int personId, String fnr, LocalDate sistOppdatert) {
        db.update(
                "INSERT INTO OPPFOLGINGSBRUKER " +
                        "(PERSON_ID, FODSELSNR, ETTERNAVN, FORNAVN, FORMIDLINGSGRUPPEKODE, KVALIFISERINGSGRUPPEKODE," +
                        " RETTIGHETSGRUPPEKODE, ER_DOED, TIDSSTEMPEL)" +
                        " VALUES (?, ?, '-', '-', '-', '-', '-', '-', ?)",
                personId, fnr, sistOppdatert);
    }
}
