package no.nav.veilarbarena.utils;

import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EnhetId;
import no.nav.pto_schema.enums.arena.*;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV1;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV2;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;
import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;
import no.nav.veilarbarena.controller.response.YtelserDTO;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbarena.utils.DateUtils.convertToLocalDate;
import static no.nav.veilarbarena.utils.EnumUtils.safeValueOf;

public class DtoMapper {

    public static YtelserDTO mapTilYtelserDTO(YtelseskontraktResponse ytelseskontraktResponse) {
        List<YtelserDTO.Vedtak> vedtakListe = ytelseskontraktResponse.getVedtaksliste()
                .stream()
                .map(vedtak -> new YtelserDTO.Vedtak()
                        .setType(vedtak.getVedtakstype())
                        .setStatus(vedtak.getStatus())
                        .setAktivitetsfase(vedtak.getAktivitetsfase())
                        .setRettighetsgruppe(vedtak.getRettighetsgruppe())
                        .setFraDato(convertToLocalDate(vedtak.getFraDato()))
                        .setTilDato(convertToLocalDate(vedtak.getTilDato())))
                .collect(Collectors.toList());

        List<YtelserDTO.Ytelseskontrakt> ytelseskontraktListe = ytelseskontraktResponse.getYtelser()
                .stream()
                .map(ytelse -> new YtelserDTO.Ytelseskontrakt()
                        .setType(ytelse.getYtelsestype())
                        .setStatus(ytelse.getStatus())
                        .setMotattDato(convertToLocalDate(ytelse.getMotattDato()))
                        .setFraDato(convertToLocalDate(ytelse.getFraDato()))
                        .setTilDato(convertToLocalDate(ytelse.getTilDato())))
                .collect(Collectors.toList());

        return new YtelserDTO(vedtakListe, ytelseskontraktListe);
    }

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

    public static EndringPaaOppfoelgingsBrukerV1 tilEndringPaaOppfoelgingsBrukerV1(OppfolgingsbrukerEntity bruker, AktorId aktorId) {
        return EndringPaaOppfoelgingsBrukerV1.builder()
                .fornavn(bruker.getFornavn())
                .aktoerid(aktorId.get())
                .etternavn(bruker.getEtternavn())
                .fodselsnr(bruker.getFodselsnr())
                .formidlingsgruppekode(bruker.getFormidlingsgruppekode())
                .iserv_fra_dato(bruker.getIservFraDato())
                .nav_kontor(bruker.getNavKontor())
                .kvalifiseringsgruppekode(bruker.getKvalifiseringsgruppekode())
                .rettighetsgruppekode(bruker.getRettighetsgruppekode())
                .hovedmaalkode(bruker.getHovedmaalkode())
                .sikkerhetstiltak_type_kode(bruker.getSikkerhetstiltakTypeKode())
                .fr_kode(bruker.getFrKode())
                .har_oppfolgingssak(bruker.getHarOppfolgingssak())
                .sperret_ansatt(bruker.getSperretAnsatt())
                .er_doed(bruker.getErDoed())
                .doed_fra_dato(bruker.getDoedFraDato())
                .endret_dato(bruker.getTimestamp())
                .build();
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
