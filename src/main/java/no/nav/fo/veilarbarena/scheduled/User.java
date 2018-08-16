package no.nav.fo.veilarbarena.scheduled;

import lombok.Value;
import no.nav.fo.veilarbarena.domain.PersonId;

import java.time.ZonedDateTime;

@Value
public class User {
    PersonId.AktorId aktoerid;
    PersonId.Fnr fodselsnr;
    String etternavn;
    String fornavn;
    String nav_kontor;
    String formidlingsgruppekode;
    ZonedDateTime iserv_fra_dato;
    String kvalifiseringsgruppekode;
    String rettighetsgruppekode;
    String hovedmaalkode;
    String sikkerhetstiltak_type_kode;
    String fr_kode;
    String har_oppfolgingssak;
    String sperret_ansatt;
    Boolean er_doed;
    ZonedDateTime doed_fra_dato;
    ZonedDateTime tidsstempel;
}
