package no.nav.veilarbarena.client.ytelseskontrakt;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class YtelseskontraktClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @SneakyThrows
    @Test
    public void hentYtelseskontraktListe_skal_returnere_innhold() {
        String apiUrl = "http://localhost:" + wireMockRule.port() + "/ail_ws/Ytelseskontrakt_v3";
        String fnr = "3628714324";
        String xmlResponse = TestUtils.readTestResourceFile("client/ytelseskontrakt/ytelseskontraktliste.xml");

        givenThat(post(urlEqualTo("/ail_ws/Ytelseskontrakt_v3"))
                .withHeader("Content-Type", containing("text/xml"))
                .withHeader("SOAPAction", equalTo("\"http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/Ytelseskontrakt_v3/hentYtelseskontraktListeRequest\""))
                .withRequestBody(matchingXPath("//personidentifikator", containing(fnr)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(xmlResponse))
        );
        var client = new YtelseskontraktClientImpl(apiUrl); // uten STS autentisering

        var datatypeFactory = DatatypeFactory.newInstance();
        var now  = datatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
        var yesterday = (XMLGregorianCalendar)now.clone();
        yesterday.setDay(now.getDay() - 1);

        YtelseskontraktResponse ytelseskontraktResponse = client.hentYtelseskontraktListe(Fnr.of(fnr), yesterday, now);

        assertThat(ytelseskontraktResponse).isNotNull();

        List<YtelseskontraktResponse.VedtakDto> vedtaksliste = ytelseskontraktResponse.getVedtaksliste();
        assertThat(vedtaksliste).hasSize(1).allMatch(ytelse -> Objects.equals(ytelse.getStatus(), "Godkjent"));
        List<YtelseskontraktResponse.YtelseskontraktDto> ytelser = ytelseskontraktResponse.getYtelser();
        assertThat(ytelser).hasSize(2)
                .anyMatch(ytelse -> Objects.equals(ytelse.getStatus(), "Aktiv"))
                .anyMatch(ytelse -> Objects.equals(ytelse.getStatus(), "Inaktiv"));
    }

    @SneakyThrows
    @Test
    public void hentYtelseskontraktListe_kan_kaste_sikkerhetsbegrensning() {
        String apiUrl = "http://localhost:" + wireMockRule.port() + "/ail_ws/Ytelseskontrakt_v3";
        String fnr = "3628714324";
        String xmlResponse = TestUtils.readTestResourceFile("client/ytelseskontrakt/sikkerhetsbegrensning.xml");

        givenThat(post(urlEqualTo("/ail_ws/Ytelseskontrakt_v3"))
                .withHeader("Content-Type", containing("text/xml"))
                .withHeader("SOAPAction", equalTo("\"http://nav.no/tjeneste/virksomhet/ytelseskontrakt/v3/Ytelseskontrakt_v3/hentYtelseskontraktListeRequest\""))
                .withRequestBody(matchingXPath("//personidentifikator", containing(fnr)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(xmlResponse))
        );
        var client = new YtelseskontraktClientImpl(apiUrl); // uten STS autentisering

        Fnr fnrType = Fnr.of(fnr);
        assertThatThrownBy(() -> client.hentYtelseskontraktListe(fnrType)).isInstanceOf(ResponseStatusException.class);
    }

}