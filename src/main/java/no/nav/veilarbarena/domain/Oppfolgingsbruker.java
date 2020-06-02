package no.nav.veilarbarena.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class Oppfolgingsbruker {

    String fornavn;
    String etternavn;
    String fodselsnr;
    String formidlingsgruppekode;
    ZonedDateTime iservFraDato;
    String navKontor;
    String kvalifiseringsgruppekode;
    String rettighetsgruppekode;
    String hovedmaalkode;
    String sikkerhetstiltakTypeKode;
    String frKode;
    Boolean harOppfolgingssak;
    Boolean sperretAnsatt;
    Boolean erDoed;
    ZonedDateTime doedFraDato;
    ZonedDateTime timestamp; // Når brukeren sist ble endret

}

