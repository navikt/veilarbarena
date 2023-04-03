package no.nav.veilarbarena.client.ytelseskontrakt;

import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.Bruker;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.Rettighetsgruppe;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.Vedtak;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.Ytelseskontrakt;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.meldinger.HentYtelseskontraktListeResponse;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class YtelseskontraktMapper {

    private YtelseskontraktMapper() {}

    public static YtelseskontraktResponse tilYtelseskontrakt(HentYtelseskontraktListeResponse response) {
        final List<YtelseskontraktResponse.VedtakDto> vedtakDtoList = mapVedtak(response);
        final List<YtelseskontraktResponse.YtelseskontraktDto> ytelser = mapYtelser(response);
        return new YtelseskontraktResponse(vedtakDtoList, ytelser);
    }

    private static List<YtelseskontraktResponse.YtelseskontraktDto> mapYtelser(HentYtelseskontraktListeResponse response) {
        return response.getYtelseskontraktListe().stream()
                .map(wsYtelseskontraktToYtelseskontrakt)
                .toList();
    }

    private static List<YtelseskontraktResponse.VedtakDto> mapVedtak(HentYtelseskontraktListeResponse response) {
        final List<Vedtak> wsVedtakList = response.getYtelseskontraktListe().stream()
                .map(Ytelseskontrakt::getIhtVedtak)
                .flatMap(Collection::stream)
                .toList();

        final List<YtelseskontraktResponse.VedtakDto> vedtakDtoList = wsVedtakList.stream()
                .map(wsVedtakToVedtak)
                .toList();

        setRettighetsgruppePaVedtak(response, vedtakDtoList);
        return vedtakDtoList;
    }

    private static void setRettighetsgruppePaVedtak(HentYtelseskontraktListeResponse response, List<YtelseskontraktResponse.VedtakDto> vedtakDtoList) {
        final String rettighetsgruppe = getRettighetsgruppe(response);

        vedtakDtoList.forEach(vedtakDto -> vedtakDto.setRettighetsgruppe(rettighetsgruppe));
    }

    private static String getRettighetsgruppe(HentYtelseskontraktListeResponse response) {
        return Optional.of(response)
                .map(HentYtelseskontraktListeResponse::getBruker)
                .map(Bruker::getRettighetsgruppe)
                .map(Rettighetsgruppe::getRettighetsGruppe).orElse("");
    }

    private static final Function<Vedtak, YtelseskontraktResponse.VedtakDto> wsVedtakToVedtak = wsVedtak -> {
        final Optional<XMLGregorianCalendar> fomdato = Optional.ofNullable(wsVedtak.getVedtaksperiode().getFom());
        final Optional<XMLGregorianCalendar> tomdato = Optional.ofNullable(wsVedtak.getVedtaksperiode().getTom());

        var ytelse = new YtelseskontraktResponse.VedtakDto()
                .setVedtakstype(wsVedtak.getVedtakstype())
                .setStatus(wsVedtak.getStatus())
                .setAktivitetsfase(wsVedtak.getAktivitetsfase());

        fomdato.ifPresent(ytelse::setFraDato);
        tomdato.ifPresent(ytelse::setTilDato);

        return ytelse;
    };

    private static final Function<Ytelseskontrakt, YtelseskontraktResponse.YtelseskontraktDto> wsYtelseskontraktToYtelseskontrakt = wsYtelseskontrakt -> {
        final Optional<XMLGregorianCalendar> fomGyldighetsperiode = Optional.ofNullable(wsYtelseskontrakt.getFomGyldighetsperiode());
        final Optional<XMLGregorianCalendar> tomGyldighetsperiode = Optional.ofNullable(wsYtelseskontrakt.getTomGyldighetsperiode());

        var ytelseskontrakt = new YtelseskontraktResponse.YtelseskontraktDto()
                .setYtelsestype(wsYtelseskontrakt.getYtelsestype())
                .setStatus(wsYtelseskontrakt.getStatus())
                .setMotattDato(wsYtelseskontrakt.getDatoKravMottatt());

        fomGyldighetsperiode.ifPresent(ytelseskontrakt::setFraDato);
        tomGyldighetsperiode.ifPresent(ytelseskontrakt::setTilDato);

        return ytelseskontrakt;
    };
}
