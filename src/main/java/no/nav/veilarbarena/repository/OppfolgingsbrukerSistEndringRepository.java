package no.nav.veilarbarena.repository;

import lombok.SneakyThrows;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerSistEndretEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

import static no.nav.veilarbarena.utils.DateUtils.convertTimestampToZonedDateTimeIfPresent;

@Repository
public class OppfolgingsbrukerSistEndringRepository {

    private final JdbcTemplate db;

    @Autowired
    public OppfolgingsbrukerSistEndringRepository(JdbcTemplate db) {
        this.db = db;
    }

    public OppfolgingsbrukerSistEndretEntity hentSistEndret(){
        String sql = "SELECT * FROM METADATA";
        return db.queryForObject(sql, OppfolgingsbrukerSistEndringRepository::mapOppfolgingsbrukerSistEndret);
    }

    public void updateLastcheck(String fnr, ZonedDateTime tidspunkt) {
        String sql = "UPDATE METADATA SET FODSELSNR = ?, OPPFOLGINGSBRUKER_SIST_ENDRING = ?";
        db.update(sql, fnr, Timestamp.from(tidspunkt.toInstant()));
    }

    @SneakyThrows
    private static OppfolgingsbrukerSistEndretEntity mapOppfolgingsbrukerSistEndret(ResultSet rs, int row) {
        return new OppfolgingsbrukerSistEndretEntity()
                .setFodselsnr(rs.getString("FODSELSNR"))
                .setOppfolgingsbrukerSistEndring(convertTimestampToZonedDateTimeIfPresent(rs.getTimestamp("OPPFOLGINGSBRUKER_SIST_ENDRING")));
    }

}
