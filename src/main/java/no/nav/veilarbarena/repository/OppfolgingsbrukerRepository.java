package no.nav.veilarbarena.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.domain.Oppfolgingsbruker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.nav.veilarbarena.utils.DateUtils.convertTimestampToZonedDateTimeIfPresent;

@Slf4j
@Repository
public class OppfolgingsbrukerRepository {

    private final JdbcTemplate db;

    private static final String ARBEIDSOKER = "ARBS";
    private static final Set<String> OPPFOLGINGKODER = new HashSet<>(asList("BATT", "BFORM", "IKVAL", "VURDU", "OPPFI", "VARIG"));
    private static final String IKKE_ARBEIDSSOKER = "IARBS";

    @Autowired
    public OppfolgingsbrukerRepository(JdbcTemplate db) {
        this.db = db;
    }

    public Optional<Oppfolgingsbruker> hentOppfolgingsbruker(String fnr){
        String sql = "SELECT * FROM OPPFOLGINGSBRUKER WHERE fodselsnr = ?";
        List<Oppfolgingsbruker> brukere = db.query(sql, new Object[]{fnr}, OppfolgingsbrukerRepository::mapOppfolgingsbruker);
        return brukere.isEmpty() ? Optional.empty() : Optional.of(brukere.get(0));
    }

    public List<Oppfolgingsbruker> hentOppfolgingsbrukere(List<String> fnrs) {
        String sql = "SELECT * FROM OPPFOLGINGSBRUKER WHERE FODSELSNR in (?)";
        return db.query(sql, new Object[]{String.join(",", fnrs)}, OppfolgingsbrukerRepository::mapOppfolgingsbruker);
    }

    public List<Oppfolgingsbruker> changesSinceLastCheckSql(String lastCheckedFnr, ZonedDateTime sistSjekketTidspunkt) {
        log.info("Siste sjekket tidspunkt: {}", sistSjekketTidspunkt);

        String tidOgFnrSql = "tidsstempel > ? OR (fodselsnr > ? AND tidsstempel >= ?)";
        String erUnderOppfolgingSql = format(
                "FORMIDLINGSGRUPPEKODE = %s OR (FORMIDLINGSGRUPPEKODE = %s AND KVALIFISERINGSGRUPPEKODE in (%s)",
                ARBEIDSOKER, IKKE_ARBEIDSSOKER, String.join(",", OPPFOLGINGKODER)
                );

        String sql = format(
                "SELECT * FROM OPPFOLGINGSBRUKER WHERE (%s) AND (%s) ORDER BY tidsstempel, fodselsnr ASC LIMIT 1000",
                tidOgFnrSql, erUnderOppfolgingSql
                );

        return db.query(
                sql,
                new Object[]{lastCheckedFnr, Timestamp.from(sistSjekketTidspunkt.toInstant())},
                OppfolgingsbrukerRepository::mapOppfolgingsbruker
        );
    }

    @SneakyThrows
    private static Oppfolgingsbruker mapOppfolgingsbruker(ResultSet rs, int row) {
        return new Oppfolgingsbruker()
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
