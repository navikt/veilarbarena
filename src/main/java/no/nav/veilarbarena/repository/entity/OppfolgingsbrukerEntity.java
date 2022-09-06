package no.nav.veilarbarena.repository.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class OppfolgingsbrukerEntity {
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
    String personId;
}

