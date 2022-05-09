package no.nav.veilarbarena.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.ArenaOrdsClientImpl;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArenaOrdsClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void hentArenaAktiviteter_skal_parse_response() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        String fnr = "3628714324";
        String xmlResponse = TestUtils.readTestResourceFile("client/ords/aktiviteter_response.xml", StandardCharsets.ISO_8859_1);

        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlEqualTo("/arena/api/v1/person/oppfoelging/aktiviteter"))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .withHeader("fnr", equalTo(fnr))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(xmlResponse))
        );

        Optional<ArenaAktiviteterDTO> maybeAktiviteter = client.hentArenaAktiviteter(Fnr.of(fnr));

        assertTrue(maybeAktiviteter.isPresent());

        ArenaAktiviteterDTO.Response response = maybeAktiviteter.get().getResponse();

        assertEquals(2, response.getGruppeaktivitetListe().size());
        assertEquals(2, response.getTiltaksaktivitetListe().size());
        assertEquals(2, response.getUtdanningsaktivitetListe().size());

        // =========================================

        ArenaAktiviteterDTO.Gruppeaktivitet gruppeaktivitet = response.getGruppeaktivitetListe()
                .stream()
                .filter(a -> a.getAktivitetId().equals("XX3463278432"))
                .findFirst()
                .orElseThrow();

        assertEquals("informasjonsmøte ved NAV lokalt", gruppeaktivitet.getAktivitetstype());
        assertEquals("Informasjonsmøte med NAV", gruppeaktivitet.getBeskrivelse());
        assertEquals("AVBR", gruppeaktivitet.getStatus());

        ArenaAktiviteterDTO.Gruppeaktivitet.MoteplanListe moteplanListe = gruppeaktivitet.getMoeteplanListe();

        assertEquals(LocalDate.of(2018, 6, 22), moteplanListe.getStartDato());
        assertEquals("13:00:00", moteplanListe.getStartKlokkeslett());
        assertEquals(LocalDate.of(2018, 6, 22), moteplanListe.getSluttDato());
        assertEquals("15:00:00", moteplanListe.getSluttKlokkeslett());
        assertEquals("NAV Testheim", moteplanListe.getSted());

        // =========================================

        ArenaAktiviteterDTO.Tiltaksaktivitet tiltaksaktivitet = response.getTiltaksaktivitetListe()
                .stream()
                .filter(a -> a.getAktivitetId().equals("XX4367922"))
                .findFirst()
                .orElseThrow();

        assertEquals("Arbeidstrening", tiltaksaktivitet.getTiltaksnavn());
        assertEquals("Hjelpearbeider - murer", tiltaksaktivitet.getTiltakLokaltNavn());
        assertEquals("TEST Muligheter AS", tiltaksaktivitet.getArrangoer());
        assertEquals("123543546", tiltaksaktivitet.getBedriftsnummer());
        assertEquals(Integer.valueOf(100), tiltaksaktivitet.getDeltakelseProsent());
        assertEquals(LocalDate.of(2017, 4, 11), tiltaksaktivitet.getStatusSistEndret());
        assertEquals("Skal bli murer", tiltaksaktivitet.getBegrunnelseInnsoeking());

        assertEquals(LocalDate.of(2017, 1, 28), tiltaksaktivitet.getDeltakelsePeriode().getFom());
        assertEquals(LocalDate.of(2017, 4, 10), tiltaksaktivitet.getDeltakelsePeriode().getTom());

        // =========================================

        ArenaAktiviteterDTO.Utdanningsaktivitet utdanningsaktivitet = response.getUtdanningsaktivitetListe()
                .stream()
                .filter(a -> a.getAktivitetId().equals("XX348702"))
                .findFirst()
                .orElseThrow();

        assertEquals("Ordinær utdanning for enslige forsørgere mv", utdanningsaktivitet.getAktivitetstype());
        assertEquals("Avsluttet arbeidsforholdet som lærling 28.02.18.", utdanningsaktivitet.getBeskrivelse());

        assertEquals(LocalDate.of(2017, 10, 1), utdanningsaktivitet.getAktivitetPeriode().getFom());
        assertEquals(LocalDate.of(2018, 2, 28), utdanningsaktivitet.getAktivitetPeriode().getTom());
    }

}
