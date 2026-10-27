package no.nav.veilarbarena.utils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto_schema.enums.arena.*;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV2;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;
import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbarena.utils.EnumUtils.safeValueOf;

public class DtoMapper {

    public static OppfolgingsstatusDTO mapTilOppfolgingsstatusDTO(ArenaOppfolgingsstatusDTO statusDto) {
        OppfolgingsstatusDTO dto = new OppfolgingsstatusDTO();
        dto.setRettighetsgruppe(statusDto.getRettighetsgruppeKode());
        dto.setFormidlingsgruppe(statusDto.getFormidlingsgruppeKode());
        dto.setServicegruppe(statusDto.getServicegruppeKode());
        dto.setOppfolgingsenhet(statusDto.getNavOppfoelgingsenhet());
        dto.setInaktiveringsdato(statusDto.getInaktiveringsdato());
        dto.setKanEnkeltReaktiveres(statusDto.getKanEnkeltReaktiveres());
        return dto;
    }

    public static OppfolgingssakDTO mapTilOppfolgingssakDTO(ArenaOppfolgingssakDTO sakDto) {
        OppfolgingssakDTO dto = new OppfolgingssakDTO();
        dto.setOppfolgingssakId(sakDto.getSaksId());
        return dto;
    }

    public static ArenaStatusDTO mapTilArenaStatusDTO(ArenaOppfolgingsstatusDTO statusDto) {
        return new ArenaStatusDTO()
                .setFormidlingsgruppe(statusDto.getFormidlingsgruppeKode())
                .setKvalifiseringsgruppe(statusDto.getServicegruppeKode())
                .setRettighetsgruppe(statusDto.getRettighetsgruppeKode())
                .setIservFraDato(statusDto.getInaktiveringsdato())
                .setOppfolgingsenhet(
                        ofNullable(statusDto.getNavOppfoelgingsenhet()).map(EnhetId::of).orElse(null)
                );
    }

    public static ArenaStatusDTO mapTilArenaStatusDTO(OppfolgingsbrukerEntity oppfolgingsbruker) {
        return new ArenaStatusDTO()
                .setFormidlingsgruppe(oppfolgingsbruker.getFormidlingsgruppekode())
                .setKvalifiseringsgruppe(oppfolgingsbruker.getKvalifiseringsgruppekode())
                .setRettighetsgruppe(oppfolgingsbruker.getRettighetsgruppekode())
                .setIservFraDato(
                        ofNullable(oppfolgingsbruker.getIservFraDato()).map(ZonedDateTime::toLocalDate).orElse(null)
                )
                .setOppfolgingsenhet(
                        ofNullable(oppfolgingsbruker.getNavKontor()).map(EnhetId::of).orElse(null)
                );
    }

    public static EndringPaaOppfoelgingsBrukerV2 tilEndringPaaOppfoelgingsBrukerV2(OppfolgingsbrukerEntity bruker) {
        LocalDate iservFraDato = ofNullable(bruker.getIservFraDato()).map(ZonedDateTime::toLocalDate).orElse(null);
        LocalDate doedFraDato = ofNullable(bruker.getDoedFraDato()).map(ZonedDateTime::toLocalDate).orElse(null);

        return EndringPaaOppfoelgingsBrukerV2.builder()
                .fornavn(bruker.getFornavn())
                .etternavn(bruker.getEtternavn())
                .fodselsnummer(bruker.getFodselsnr())
                .formidlingsgruppe(safeValueOf(Formidlingsgruppe.class, bruker.getFormidlingsgruppekode()))
                .iservFraDato(iservFraDato)
                .oppfolgingsenhet(bruker.getNavKontor())
                .kvalifiseringsgruppe(safeValueOf(Kvalifiseringsgruppe.class, bruker.getKvalifiseringsgruppekode()))
                .rettighetsgruppe(safeValueOf(Rettighetsgruppe.class, bruker.getRettighetsgruppekode()))
                .hovedmaal(safeValueOf(Hovedmaal.class, bruker.getHovedmaalkode()))
                .sikkerhetstiltakType(safeValueOf(SikkerhetstiltakType.class, bruker.getSikkerhetstiltakTypeKode()))
                .diskresjonskode(bruker.getFrKode())
                .harOppfolgingssak(bruker.getHarOppfolgingssak())
                .sperretAnsatt(bruker.getSperretAnsatt())
                .erDoed(bruker.getErDoed())
                .doedFraDato(doedFraDato)
                .sistEndretDato(bruker.getTimestamp())
                .build();
    }

}
