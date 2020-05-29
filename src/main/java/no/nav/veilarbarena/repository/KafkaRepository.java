package no.nav.veilarbarena.repository;

import lombok.SneakyThrows;
import no.nav.veilarbarena.domain.FeiletKafkaRecord;
import no.nav.veilarbarena.domain.User;
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

    public void insertFeiletBruker(User user) {
        String sql = "INSERT INTO FEILEDE_KAFKA_BRUKERE(FODSELSNR, TIDSPUNKT_FEILET) values(?, CURRENT_TIMESTAMP)";
        db.update(sql, user.getFodselsnr().get());
    }

    public void deleteFeiletBruker(User user) {
        String sql = "DELETE FROM FEILEDE_KAFKA_BRUKERE WHERE FODSELSNR = ?";
        db.update(sql, user.getFodselsnr().get());
    }

    public List<FeiletKafkaRecord> hentFeiledeBrukere() {
        String sql = "SELECT * FROM FEILEDE_KAFKA_BRUKERE LIMIT 1000";
        return db.query(sql, KafkaRepository::mapFeiletKafkaRecord);
    }

    @SneakyThrows
    private static FeiletKafkaRecord mapFeiletKafkaRecord(ResultSet rs, int row) {
        return new FeiletKafkaRecord(rs.getString("FODSELSNR"), rs.getTimestamp("TIDSPUNKT_FEILET").toLocalDateTime());
    }

}
