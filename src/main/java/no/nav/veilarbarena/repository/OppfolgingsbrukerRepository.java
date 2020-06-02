package no.nav.veilarbarena.repository;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.domain.UserRecord;
import no.nav.veilarbarena.domain.api.OppfolgingsbrukerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.ZoneId;
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

    public Optional<OppfolgingsbrukerDTO> hentOppfolgingsbruker(String fnr){
        String sql = "SELECT * FROM OPPFOLGINGSBRUKER WHERE fodselsnr = ?";
        List<OppfolgingsbrukerDTO> brukere = db.query(sql, new Object[]{fnr}, OppfolgingsbrukerRepository::mapOppfolgingsbruker);
        return brukere.isEmpty() ? Optional.empty() : Optional.of(brukere.get(0));
    }

    public List<OppfolgingsbrukerDTO> hentOppfolgingsbrukere(List<String> fnrs) {
        String sql = "SELECT * FROM OPPFOLGINGSBRUKER WHERE FODSELSNR in (?)";
        return db.query(sql, new Object[]{String.join(",", fnrs)}, OppfolgingsbrukerRepository::mapOppfolgingsbruker);
    }

    public List<OppfolgingsbrukerDTO> changesSinceLastCheckSql(String lastCheckedFnr, ZonedDateTime sistSjekketTidspunkt) {
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
    private static OppfolgingsbrukerDTO mapOppfolgingsbruker(ResultSet rs, int row) {
        return OppfolgingsbrukerDTO.builder()
                .fodselsnr(rs.getString("fodselsnr"))
                .formidlingsgruppekode(rs.getString("formidlingsgruppekode"))
                .iserv_fra_dato(convertTimestampToZonedDateTimeIfPresent(rs.getTimestamp("iserv_fra_dato")))
                .nav_kontor(rs.getString("nav_kontor"))
                .kvalifiseringsgruppekode(rs.getString("kvalifiseringsgruppekode"))
                .rettighetsgruppekode(rs.getString("rettighetsgruppekode"))
                .hovedmaalkode(rs.getString("hovedmaalkode"))
                .sikkerhetstiltak_type_kode(rs.getString("sikkerhetstiltak_type_kode"))
                .fr_kode(rs.getString("fr_kode"))
                .har_oppfolgingssak(convertStringToBoolean(rs.getString("har_oppfolgingssak")))
                .sperret_ansatt(convertStringToBoolean(rs.getString("sperret_ansatt")))
                .er_doed(convertStringToBoolean(rs.getString("er_doed")))
                .doed_fra_dato(convertTimestampToZonedDateTimeIfPresent(rs.getTimestamp("doed_fra_dato")))
                .build();
    }

    private static boolean convertStringToBoolean(String flag){
        return Optional.ofNullable(flag).isPresent() && flag.equals("J");
    }

}
