package no.nav.veilarbarena.repository;

import lombok.SneakyThrows;
import no.nav.veilarbarena.domain.FeiletKafkaBruker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

@Repository
public class KafkaRepository {

    private final JdbcTemplate db;

    @Autowired
    public KafkaRepository(JdbcTemplate db) {
        this.db = db;
    }

    public void insertFeiletBruker(String brukerFnr) {
        String sql = "INSERT INTO FEILEDE_KAFKA_BRUKERE(FODSELSNR, TIDSPUNKT_FEILET) values(?, CURRENT_TIMESTAMP)";
        db.update(sql, brukerFnr);
    }

    public void deleteFeiletBruker(String brukerFnr) {
        String sql = "DELETE FROM FEILEDE_KAFKA_BRUKERE WHERE FODSELSNR = ?";
        db.update(sql, brukerFnr);
    }

    public List<FeiletKafkaBruker> hentFeiledeBrukere() {
        String sql = "SELECT * FROM FEILEDE_KAFKA_BRUKERE LIMIT 1000";
        return db.query(sql, KafkaRepository::mapFeiletKafkaRecord);
    }

    @SneakyThrows
    private static FeiletKafkaBruker mapFeiletKafkaRecord(ResultSet rs, int row) {
        return new FeiletKafkaBruker(rs.getString("FODSELSNR"), rs.getTimestamp("TIDSPUNKT_FEILET").toLocalDateTime());
    }

}
