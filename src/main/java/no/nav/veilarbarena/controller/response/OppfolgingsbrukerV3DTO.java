package no.nav.veilarbarena.controller.response;

import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;

import java.time.ZonedDateTime;

public record OppfolgingsbrukerV3DTO(
        String fodselsnr,
        String formidlingsgruppekode,
        ZonedDateTime iservFraDato,
        String navKontor,
        String kvalifiseringsgruppekode,
        String rettighetsgruppekode,
        String hovedmaalkode,
        String sikkerhetstiltakTypeKode,
        String frKode,
        Boolean harOppfolgingssak,
        Boolean sperretAnsatt,
        Boolean erDoed,
        ZonedDateTime doedFraDato,
        ZonedDateTime sistEndretDato
) {

    public static OppfolgingsbrukerV3DTO fraOppfolgingsbruker(OppfolgingsbrukerEntity bruker) {
        return new OppfolgingsbrukerV3DTO(
                bruker.getFodselsnr(),
                bruker.getFormidlingsgruppekode(),
                bruker.getIservFraDato(),
                bruker.getNavKontor(),
                bruker.getKvalifiseringsgruppekode(),
                bruker.getRettighetsgruppekode(),
                bruker.getHovedmaalkode(),
                bruker.getSikkerhetstiltakTypeKode(),
                bruker.getFrKode(),
                bruker.getHarOppfolgingssak(),
                bruker.getSperretAnsatt(),
                bruker.getErDoed(),
                bruker.getDoedFraDato(),
                bruker.getTimestamp()
        );
    }
}
