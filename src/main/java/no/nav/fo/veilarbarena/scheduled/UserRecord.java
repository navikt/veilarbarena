package no.nav.fo.veilarbarena.scheduled;

import lombok.Value;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.sbl.sql.mapping.QueryMapping.Column;
import no.nav.sbl.sql.mapping.SqlRecord;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Value
public class UserRecord implements SqlRecord {
    Column<String, PersonId.Fnr> fodselsnr;
    Column<String, String> etternavn;
    Column<String, String> fornavn;
    Column<String, String> nav_kontor;
    Column<String, String> formidlingsgruppekode;
    Column<Timestamp, ZonedDateTime> iserv_fra_dato;
    Column<String, String> kvalifiseringsgruppekode;
    Column<String, String> rettighetsgruppekode;
    Column<String, String> hovedmaalkode;
    Column<String, String> sikkerhetstiltak_type_kode;
    Column<String, String> fr_kode;
    Column<String, Boolean> har_oppfolgingssak;
    Column<String, Boolean> sperret_ansatt;
    Column<String, Boolean> er_doed;
    Column<Timestamp, ZonedDateTime> doed_fra_dato;
    Column<Timestamp, ZonedDateTime> tidsstempel;
}
