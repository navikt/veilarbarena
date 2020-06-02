package no.nav.veilarbarena.domain.api;

import lombok.*;
import lombok.experimental.Accessors;
import no.nav.veilarbarena.domain.Oppfolgingsbruker;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class OppfolgingsbrukerEndretDTO {

    String aktoerid;
    String fodselsnr;
    String formidlingsgruppekode;
    ZonedDateTime iserv_fra_dato;
    String etternavn;
    String fornavn;
    String nav_kontor;
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
    
    public static OppfolgingsbrukerEndretDTO fraOppfolgingsbruker(Oppfolgingsbruker bruker) {
        return new OppfolgingsbrukerEndretDTO()
                .setFornavn(bruker.getFornavn())
                .setEtternavn(bruker.getEtternavn())
                .setFodselsnr(bruker.getFodselsnr())
                .setFormidlingsgruppekode(bruker.getFormidlingsgruppekode())
                .setIserv_fra_dato(bruker.getIservFraDato())
                .setNav_kontor(bruker.getNavKontor())
                .setKvalifiseringsgruppekode(bruker.getKvalifiseringsgruppekode())
                .setRettighetsgruppekode(bruker.getRettighetsgruppekode())
                .setHovedmaalkode(bruker.getHovedmaalkode())
                .setSikkerhetstiltak_type_kode(bruker.getSikkerhetstiltakTypeKode())
                .setFr_kode(bruker.getFrKode())
                .setHar_oppfolgingssak(bruker.getHarOppfolgingssak())
                .setSperret_ansatt(bruker.getSperretAnsatt())
                .setEr_doed(bruker.getErDoed())
                .setDoed_fra_dato(bruker.getDoedFraDato())
                .setEndret_dato(bruker.getTimestamp());
    }

}
