package no.nav.veilarbarena.controller.response;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class OppfolgingsbrukerDTO {

    String fodselsnr;
    String formidlingsgruppekode;
    ZonedDateTime iserv_fra_dato;
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

    public static OppfolgingsbrukerDTO fraOppfolgingsbruker(OppfolgingsbrukerEntity bruker) {
        return new OppfolgingsbrukerDTO()
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
                .setDoed_fra_dato(bruker.getDoedFraDato());
    }

}

