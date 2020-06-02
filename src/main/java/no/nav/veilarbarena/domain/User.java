package no.nav.veilarbarena.domain;

import lombok.Value;
import lombok.experimental.Wither;

import java.time.ZonedDateTime;

@Value
@Wither
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
    Boolean har_oppfolgingssak;
    Boolean sperret_ansatt;
    Boolean er_doed;
    ZonedDateTime doed_fra_dato;
    ZonedDateTime endret_dato;

}
