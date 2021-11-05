package no.nav.veilarbarena.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.repository.entity.OppdatertBrukerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.function.Supplier;


@Slf4j
@Repository
public class OppdaterteBrukereRepository {

    private final JdbcTemplate db;

    @Autowired
    public OppdaterteBrukereRepository(JdbcTemplate db) {
        this.db = db;
    }


    public OppdatertBrukerEntity hentBrukerMedEldstEndring() {
        String sql = "SELECT * FROM OPPDATERTE_BRUKERE ORDER BY TIDSSTEMPEL FETCH NEXT 1 ROWS ONLY";
        return queryForObjectOrNull(() -> db.queryForObject(sql, OppdaterteBrukereRepository::mapOppfolgingsbrukerSistEndret));
    }

    public void slettOppdatering(OppdatertBrukerEntity oppdatering) {
        String sql = "DELETE FROM OPPDATERTE_BRUKERE WHERE FNR = ? AND TIDSSTEMPEL = ?";
        db.update(sql, oppdatering.getFodselsnr(), oppdatering.getTidsstempel());
    }

    /*
        Inserter alle fnr slik at naavarende tilstand blir publisert pa kafka.
        Re-publiseringen blir nedprioritert i forhold til faktiske endringer i OPPFOLGINGSBRUKER tabellen.
        pga. TIDSSTEMPEL satt til 17/12/2072
    * */
    public void insertAlleBrukereFraOppfolgingsbrukerTabellen() {
        db.update("merge into OPPDATERTE_BRUKERE t using (SELECT DISTINCT FODSELSNR FROM OPPFOLGINGSBRUKER) s" +
                "    on (t.FNR = s.FODSELSNR)" +
                "    when not matched" +
                "    then" +
                "        insert (FNR, TIDSSTEMPEL) values (s.FODSELSNR, TO_DATE('17/12/2072', 'DD/MM/YYYY'))");
    }

    public Long hentAntallBrukereSomSkalOppdaters() {
        String sql = "SELECT count(*) FROM OPPDATERTE_BRUKERE";
        return queryForObjectOrNull(() -> db.queryForObject(sql, Long.class));
    }

    @SneakyThrows
    private static OppdatertBrukerEntity mapOppfolgingsbrukerSistEndret(ResultSet rs, int row) {
        return new OppdatertBrukerEntity()
                .setFodselsnr(rs.getString("FNR"))
                .setTidsstempel(rs.getDate("TIDSSTEMPEL"));
    }

    public static <T> T queryForObjectOrNull(Supplier<T> query) {
        try {
            return query.get();
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
