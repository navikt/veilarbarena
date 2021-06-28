package no.nav.veilarbarena.utils;

import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
import no.nav.veilarbarena.controller.response.YtelserDTO;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.veilarbarena.utils.DateUtils.convertToLocalDate;

public class DtoMapper {

    public static YtelserDTO mapTilYtelserDto(YtelseskontraktResponse ytelseskontraktResponse) {
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

}
