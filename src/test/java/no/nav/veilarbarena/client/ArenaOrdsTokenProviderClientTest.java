package no.nav.veilarbarena.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.nav.common.json.JsonUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.lang.System.setProperty;
import static no.nav.veilarbarena.client.ArenaOrdsTokenProviderClient.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ArenaOrdsTokenProviderClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Before
    public void setup() {
        setProperty(ARENA_ORDS_CLIENT_ID_PROPERTY, "client_id");
        setProperty(ARENA_ORDS_CLIENT_SECRET_PROPERTY, "client_secret");
    }

    @Test
    public void henterGyldigTokenFraCache() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        ArenaOrdsTokenProviderClient tokenProvider = new ArenaOrdsTokenProviderClient(apiUrl);

        OrdsToken token1 = new OrdsToken("token1", "", 120);
        OrdsToken token2 = new OrdsToken("token2", "", 120);

        gittResponseMedToken(token1);

        String tokenRespons1 = tokenProvider.getToken();

        assertThat(tokenRespons1).isEqualTo(token1.getAccessToken());

        gittResponseMedToken(token2);

        String tokenRespons2 = tokenProvider.getToken();

        assertThat(tokenRespons2).isEqualTo(token1.getAccessToken());
    }

    @Test
    public void henterNyttTokenDersomTokenFraCacheGammelt() {
        String apiUrl = "http://localhost:" + wireMockRule.port();
        ArenaOrdsTokenProviderClient tokenProvider = new ArenaOrdsTokenProviderClient(apiUrl);

        OrdsToken token1 = new OrdsToken("token1", "", 50);
        OrdsToken token2 = new OrdsToken("token2", "", 120);

        gittResponseMedToken(token1);

        String tokenRespons1 = tokenProvider.getToken();

        assertThat(tokenRespons1).isEqualTo(token1.getAccessToken());

        gittResponseMedToken(token2);

        String tokenRespons2 = tokenProvider.getToken();

        assertThat(tokenRespons2).isEqualTo(token2.getAccessToken());
    }

    private void gittResponseMedToken(OrdsToken ordsToken) {
        givenThat(post(urlEqualTo("/arena/api/oauth/token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(JsonUtils.toJson(ordsToken)))
        );
    }
}
