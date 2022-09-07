package no.nav.veilarbarena.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static no.nav.veilarbarena.utils.DatabaseUtils.toSqlStringArray;
import static no.nav.veilarbarena.utils.DateUtils.convertTimestampToZonedDateTimeIfPresent;

@Slf4j
@Repository
public class OppfolgingsbrukerRepository {

    private final JdbcTemplate db;

    @Autowired
    public OppfolgingsbrukerRepository(JdbcTemplate db) {
        this.db = db;
    }

    public Optional<OppfolgingsbrukerEntity> hentOppfolgingsbruker(String fnr){
        String sql = "SELECT * FROM OPPFOLGINGSBRUKER WHERE fodselsnr = ?";
        List<OppfolgingsbrukerEntity> brukere = db.query(sql, OppfolgingsbrukerRepository::mapOppfolgingsbruker, fnr);
        return brukere.isEmpty() ? Optional.empty() : Optional.of(brukere.get(0));
    }

    public Optional<String> hentOppfolgingsbrukerSinPersonId(String fnr){
        String sql = "SELECT person_id FROM OPPFOLGINGSBRUKER WHERE fodselsnr = ?";
        return Optional.ofNullable(db.queryForObject(sql, String.class, fnr));
    }

    public List<OppfolgingsbrukerEntity> hentOppfolgingsbrukere(List<String> fnrs) {
        if (fnrs.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = format("SELECT * FROM OPPFOLGINGSBRUKER WHERE FODSELSNR IN %s", toSqlStringArray(fnrs));
        return db.query(sql, OppfolgingsbrukerRepository::mapOppfolgingsbruker);
    }

    public List<OppfolgingsbrukerEntity> changesSinceLastCheckSql(String lastCheckedFnr, ZonedDateTime sistSjekketTidspunkt) {
        log.info("Siste sjekket tidspunkt: {}", sistSjekketTidspunkt);

        Timestamp timestamp = Timestamp.from(sistSjekketTidspunkt.toInstant());
        String tidOgFnrSql = "tidsstempel > ? OR (fodselsnr > ? AND tidsstempel = ?)";

        String sql = format(
                "SELECT * FROM OPPFOLGINGSBRUKER WHERE (%s) ORDER BY tidsstempel, fodselsnr ASC FETCH NEXT 5000 ROWS ONLY",
                tidOgFnrSql
                );

        return db.query(
                sql,
                OppfolgingsbrukerRepository::mapOppfolgingsbruker,
                timestamp, lastCheckedFnr, timestamp
        );
    }

    public List<OppfolgingsbrukerEntity> hentBrukerePage(int offset, int pageSize) {
        String sql = format("SELECT * FROM OPPFOLGINGSBRUKER ORDER BY fodselsnr OFFSET %d ROWS FETCH NEXT %d ROWS ONLY", offset, pageSize);
        return db.query(sql, OppfolgingsbrukerRepository::mapOppfolgingsbruker);
    }

    @SneakyThrows
    private static OppfolgingsbrukerEntity mapOppfolgingsbruker(ResultSet rs, int row) {
        return new OppfolgingsbrukerEntity()
                .setFornavn(rs.getString("fornavn"))
                .setEtternavn(rs.getString("etternavn"))
                .setFodselsnr(rs.getString("fodselsnr"))
                .setFormidlingsgruppekode(rs.getString("formidlingsgruppekode"))
                .setIservFraDato(convertTimestampToZonedDateTimeIfPresent(rs.getTimestamp("iserv_fra_dato")))
                .setNavKontor(rs.getString("nav_kontor"))
                .setKvalifiseringsgruppekode(rs.getString("kvalifiseringsgruppekode"))
                .setRettighetsgruppekode(rs.getString("rettighetsgruppekode"))
                .setHovedmaalkode(rs.getString("hovedmaalkode"))
                .setSikkerhetstiltakTypeKode(rs.getString("sikkerhetstiltak_type_kode"))
                .setFrKode(rs.getString("fr_kode"))
                .setHarOppfolgingssak(convertStringToBoolean(rs.getString("har_oppfolgingssak")))
                .setSperretAnsatt(convertStringToBoolean(rs.getString("sperret_ansatt")))
                .setErDoed(convertStringToBoolean(rs.getString("er_doed")))
                .setDoedFraDato(convertTimestampToZonedDateTimeIfPresent(rs.getTimestamp("doed_fra_dato")))
                .setTimestamp(convertTimestampToZonedDateTimeIfPresent(rs.getTimestamp("tidsstempel")));
    }

    private static boolean convertStringToBoolean(String flag){
        return Optional.ofNullable(flag).isPresent() && flag.equals("J");
    }

}
