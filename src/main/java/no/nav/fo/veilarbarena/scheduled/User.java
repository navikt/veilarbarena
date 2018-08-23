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
    Boolean har_oppfolgingssak;
    Boolean sperret_ansatt;
    Boolean er_doed;
    ZonedDateTime doed_fra_dato;
    ZonedDateTime tidsstempel;

    public static User of(UserRecord record) {
        return new User(
                record.getAktoerid().value,
                record.getFodselsnr().value,
                record.getEtternavn().value,
                record.getFornavn().value,
                record.getNav_kontor().value,
                record.getFormidlingsgruppekode().value,
                record.getIserv_fra_dato().value,
                record.getKvalifiseringsgruppekode().value,
                record.getRettighetsgruppekode().value,
                record.getHovedmaalkode().value,
                record.getSikkerhetstiltak_type_kode().value,
                record.getFr_kode().value,
                record.getHar_oppfolgingssak().value,
                record.getSperret_ansatt().value,
                record.getEr_doed().value,
                record.getDoed_fra_dato().value,
                record.getTidsstempel().value
        );
    }
}
