package no.nav.veilarbarena.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.json.JsonUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.ArenaOrdsClientImpl;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;
import no.nav.veilarbarena.client.ords.dto.RegistrerIkkeArbeidssokerResponse;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

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

        ArenaAktiviteterDTO.Gruppeaktivitet.Moteplan moteplan = gruppeaktivitet.getMoeteplanListe().get(0);

        assertEquals(LocalDate.of(2018, 6, 22), moteplan.getStartDato());
        assertEquals("13:00:00", moteplan.getStartKlokkeslett());
        assertEquals(LocalDate.of(2018, 6, 22), moteplan.getSluttDato());
        assertEquals("15:00:00", moteplan.getSluttKlokkeslett());
        assertEquals("NAV Testheim", moteplan.getSted());

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
        assertEquals(Float.valueOf(100), tiltaksaktivitet.getDeltakelseProsent());
        assertEquals(Float.valueOf("4.5"), tiltaksaktivitet.getAntallDagerPerUke());
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

    @Test
    public void hentArenaAktiviteter_skal_ha_tom_liste_som_default() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        String fnr = "3628714324";
        String xmlResponse = TestUtils.readTestResourceFile("client/ords/aktiviteter_empty_response.xml", StandardCharsets.ISO_8859_1);

        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlEqualTo("/arena/api/v1/person/oppfoelging/aktiviteter"))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .withHeader("fnr", equalTo(fnr))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(xmlResponse))
        );

        Optional<ArenaAktiviteterDTO> maybeAktiviteter = client.hentArenaAktiviteter(Fnr.of(fnr));

        assertTrue(maybeAktiviteter.isPresent());

        ArenaAktiviteterDTO aktiviteter = maybeAktiviteter.get();

        assertTrue(aktiviteter.getResponse().getGruppeaktivitetListe().isEmpty());
        assertTrue(aktiviteter.getResponse().getTiltaksaktivitetListe().isEmpty());
        assertTrue(aktiviteter.getResponse().getUtdanningsaktivitetListe().isEmpty());
    }

    @Test
    public void hentArenaOppfolgingssak_skal_returnere_saksid() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        String fnr = "3628714324";
        String jsonResponse = TestUtils.readTestResourceFile("client/ords/oppfoelgingssak_arbeidssoeker.json");

        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlPathEqualTo("/arena/api/v2/person/oppfoelging/oppfoelgingssak"))
                .withHeader("fnr", equalTo(fnr))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonResponse))
        );

        Optional<ArenaOppfolgingssakDTO> arenaOppfolgingssakDTO = client.hentArenaOppfolgingssak(Fnr.of(fnr));
        assertThat(arenaOppfolgingssakDTO).isPresent().get().extracting("saksId").isEqualTo("5514902");
    }

    @Test
    public void hentArenaOppfolgingssak_uten_sak_skal_returnere_saksid_null() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        String fnr = "3628714324";
        String jsonResponse = TestUtils.readTestResourceFile("client/ords/oppfoelgingssak_uten_sak.json");

        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlPathEqualTo("/arena/api/v2/person/oppfoelging/oppfoelgingssak"))
                .withHeader("fnr", equalTo(fnr))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonResponse))
        );

        Optional<ArenaOppfolgingssakDTO> arenaOppfolgingssakDTO = client.hentArenaOppfolgingssak(Fnr.of(fnr));
        assertThat(arenaOppfolgingssakDTO).isPresent().get().extracting("saksId").isNull();
    }

    @Test
    public void hentArenaOppfolgingsstatus_arbeidssoker() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        String fnr = "3628714324";
        String jsonResponse = TestUtils.readTestResourceFile("client/ords/oppfoelgingsstatus_arbeidssoeker.json");

        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlPathEqualTo("/arena/api/v2/person/oppfoelging/oppfoelgingsstatus"))
                .withHeader("fnr", equalTo(fnr))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonResponse))
        );

        Optional<ArenaOppfolgingsstatusDTO> arenaOppfolgingsstatusDTO = client.hentArenaOppfolgingsstatus(Fnr.of(fnr));
        assertThat(arenaOppfolgingsstatusDTO).isPresent().get()
                .hasFieldOrPropertyWithValue("formidlingsgruppeKode", "ARBS")
                .hasFieldOrPropertyWithValue("servicegruppeKode", "IKVAL");
    }

    @Test
    public void registrer_ikke_arbeidssoker_returnerer_streng() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        String fnr = "3628714324";
        String response = "Ny bruker ble registrert ok som IARBS";
        RegistrerIkkeArbeidssokerResponse resultat = new RegistrerIkkeArbeidssokerResponse(response);
        String jsonResonse = JsonUtils.toJson(resultat);
        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");
        givenThat(post(urlPathEqualTo("/arena/api/v2/person/oppfoelging/registrer"))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .withRequestBody(equalToJson("{\"personident\":\"3628714324\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(jsonResonse))
        );
        Optional<RegistrerIkkeArbeidssokerResponse> result = client.registrerIkkeArbeidssoker(Fnr.of(fnr));
        assertThat(result).isPresent().get().isEqualTo(resultat);
    }

    @Test
    public void registrer_ikke_arbeidssoker_kan_feile_med_422() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        String fnr = "3628714324";
        String response = "Eksisterende bruker er ikke oppdatert da bruker kan reaktiveres forenklet som arbeidssøker";
        RegistrerIkkeArbeidssokerResponse resultat = new RegistrerIkkeArbeidssokerResponse(response);
        String jsonResonse = JsonUtils.toJson(resultat);
        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");
        givenThat(post(urlPathEqualTo("/arena/api/v2/person/oppfoelging/registrer"))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .withRequestBody(equalToJson("{\"personident\":\"3628714324\"}"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withBody(jsonResonse))
        );
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class, () -> client.registrerIkkeArbeidssoker(Fnr.of(fnr)));
        assertThat(responseStatusException.getStatusCode().value()).isEqualTo(422);
        assertThat(responseStatusException.getReason()).isEqualTo(jsonResonse);
    }

    @Test
    public void checkHealth_kaller_ping() {
        String apiUrl = "http://localhost:" + wireMockRule.port();

        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlPathEqualTo("/arena/api/v1/test/ping"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("OK"))
        );

        HealthCheckResult healthCheckResult = client.checkHealth();
        assertThat(healthCheckResult.isHealthy()).isTrue();
    }

    @Test
    public void checkHealth_skal_feile_med_result() {
        String apiUrl = "http://localhost:" + wireMockRule.port();

        ArenaOrdsClientImpl client = new ArenaOrdsClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlPathEqualTo("/arena/api/v1/test/ping"))
                .willReturn(aResponse()
                        .withStatus(500))
        );

        HealthCheckResult healthCheckResult = client.checkHealth();
        assertThat(healthCheckResult.isHealthy()).isFalse();
    }


}
