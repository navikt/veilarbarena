package no.nav.veilarbarena.client.ytelseskontrakt;

import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSBruker;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSRettighetsgruppe;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSVedtak;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.informasjon.ytelseskontrakt.WSYtelseskontrakt;
import no.nav.tjeneste.virksomhet.ytelseskontrakt.v3.meldinger.WSHentYtelseskontraktListeResponse;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class YtelseskontraktMapper {

    public static YtelseskontraktResponse tilYtelseskontrakt(WSHentYtelseskontraktListeResponse response) {
        final List<YtelseskontraktResponse.Vedtak> vedtakList = mapVedtak(response);
        final List<YtelseskontraktResponse.Ytelseskontrakt> ytelser = mapYtelser(response);
        return new YtelseskontraktResponse(vedtakList, ytelser);
    }

    private static List<YtelseskontraktResponse.Ytelseskontrakt> mapYtelser(WSHentYtelseskontraktListeResponse response) {
        return response.getYtelseskontraktListe().stream()
                .map(wsYtelseskontraktToYtelseskontrakt)
                .collect(Collectors.toList());
    }

    private static List<YtelseskontraktResponse.Vedtak> mapVedtak(WSHentYtelseskontraktListeResponse response) {
        final List<WSVedtak> wsVedtakList = response.getYtelseskontraktListe().stream()
                .map(WSYtelseskontrakt::getIhtVedtak)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        final List<YtelseskontraktResponse.Vedtak> vedtakList = wsVedtakList.stream()
                .map(wsVedtakToVedtak)
                .collect(Collectors.toList());

        setRettighetsgruppePaVedtak(response, vedtakList);
        return vedtakList;
    }

    private static void setRettighetsgruppePaVedtak(WSHentYtelseskontraktListeResponse response, List<YtelseskontraktResponse.Vedtak> vedtakList) {
        final String rettighetsgruppe = getRettighetsgruppe(response);

        vedtakList.forEach(vedtak -> vedtak.setRettighetsgruppe(rettighetsgruppe));
    }

    private static String getRettighetsgruppe(WSHentYtelseskontraktListeResponse response) {
        return Optional.of(response)
                .map(WSHentYtelseskontraktListeResponse::getBruker)
                .map(WSBruker::getRettighetsgruppe)
                .map(WSRettighetsgruppe::getRettighetsGruppe).orElse("");
    }

    private static final Function<WSVedtak, YtelseskontraktResponse.Vedtak> wsVedtakToVedtak = wsVedtak -> {
        final Optional<XMLGregorianCalendar> fomdato = Optional.ofNullable(wsVedtak.getVedtaksperiode().getFom());
        final Optional<XMLGregorianCalendar> tomdato = Optional.ofNullable(wsVedtak.getVedtaksperiode().getTom());

        var ytelse = new YtelseskontraktResponse.Vedtak()
                .setVedtakstype(wsVedtak.getVedtakstype())
                .setStatus(wsVedtak.getStatus())
                .setAktivitetsfase(wsVedtak.getAktivitetsfase());

        fomdato.ifPresent(ytelse::setFraDato);
        tomdato.ifPresent(ytelse::setTilDato);

        return ytelse;
    };

    private static final Function<WSYtelseskontrakt, YtelseskontraktResponse.Ytelseskontrakt> wsYtelseskontraktToYtelseskontrakt = wsYtelseskontrakt -> {
        final Optional<XMLGregorianCalendar> fomGyldighetsperiode = Optional.ofNullable(wsYtelseskontrakt.getFomGyldighetsperiode());
        final Optional<XMLGregorianCalendar> tomGyldighetsperiode = Optional.ofNullable(wsYtelseskontrakt.getTomGyldighetsperiode());

        var ytelseskontrakt = new YtelseskontraktResponse.Ytelseskontrakt()
                .setYtelsestype(wsYtelseskontrakt.getYtelsestype())
                .setStatus(wsYtelseskontrakt.getStatus())
                .setMotattDato(wsYtelseskontrakt.getDatoKravMottatt());

        fomGyldighetsperiode.ifPresent(ytelseskontrakt::setFraDato);
        tomGyldighetsperiode.ifPresent(ytelseskontrakt::setTilDato);

        return ytelseskontrakt;
    };
}
