package no.nav.fo.veilarbarena.service;

import io.vavr.collection.List;
import no.nav.fo.veilarbarena.domain.FeiletKafkaRecord;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.sbl.sql.DbConstants;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class OppfolgingsbrukerEndringRepository {

    @Inject
    private JdbcTemplate db;

    public void insertFeiletBruker(User user) {
        SqlUtils.insert(db, "FEILEDE_KAFKA_BRUKERE")
                .value("FODSELSNR", user.getFodselsnr().get())
                .value("TIDSPUNKT_FEILET", DbConstants.CURRENT_TIMESTAMP)
                .execute();
    }

    public void deleteFeiletBruker(User user) {
        SqlUtils.delete(db, "FEILEDE_KAFKA_BRUKERE")
                .where(WhereClause.equals("FODSELSNR", user.getFodselsnr().get()))
                .execute();
    }

    public List<FeiletKafkaRecord> hentFeiledeBrukere() {
        return List.ofAll(SqlUtils.select(db, "FEILEDE_KAFKA_BRUKERE", FeiletKafkaRecord.class)
                .limit(1000)
                .executeToList());
    }
}
