package no.nav.veilarbarena.utils;

import no.nav.common.types.identer.EnhetId;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV1;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;
import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;
import no.nav.veilarbarena.controller.response.YtelserDTO;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.service.dto.ArenaOppfolgingsstatusDTO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static no.nav.veilarbarena.utils.DateUtils.convertToLocalDate;

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

    public static EndringPaaOppfoelgingsBrukerV1 tilEndringPaaOppfoelgingsBrukerV1(OppfolgingsbrukerEntity bruker) {
        return new EndringPaaOppfoelgingsBrukerV1()
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
