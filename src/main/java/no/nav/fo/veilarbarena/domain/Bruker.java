package no.nav.fo.veilarbarena.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@ToString
public class Bruker {
    Long person_id;
    String aktoerid;
    String fodselsnr;
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
    boolean er_doed;
    ZonedDateTime doed_fra_dato;
    ZonedDateTime tidsstempel;
}
