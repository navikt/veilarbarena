package no.nav.veilarbarena.client.unleash;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertTrue;

public class VeilarbaktivitetUnleashClientImplTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void hentArenaAktiviteter_skal_parse_response() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        VeilarbaktivitetUnleashClient client = new VeilarbaktivitetUnleashClientImpl(apiUrl, () -> "TEST");

        givenThat(get(urlEqualTo("/veilarbaktivitet/api/feature?feature=" + VeilarbaktivitetUnleashClientImpl.featureToggleName))
                .withHeader("Authorization", equalTo("Bearer TEST"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "veilarbarena.oppfolgingsbrukerBatchDisabled": true }
                                """))
        );

        Optional<Boolean> maybeAktiviteter = client.oppfolgingsbrukerBatchIsDisabled();
        assertTrue(maybeAktiviteter.isPresent());
        assertTrue(maybeAktiviteter.get());
    }

}