package no.nav.veilarbarena.utils;

import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static java.lang.System.setProperty;
import static no.nav.veilarbarena.utils.ArenaOrdsTokenProvider.*;
import static no.nav.veilarbarena.utils.ArenaOrdsUrl.ARENA_ORDS_URL_PROPERTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArenaOrdsTokenProviderTest {

    private OkHttpClient client = mock(OkHttpClient.class);
    private WebTarget webTarget = mock(WebTarget.class);
    private Builder requestBuilder = mock(Builder.class);
    private Response response = mock(Response.class);

    private ArenaOrdsTokenProvider tokenProvider = new ArenaOrdsTokenProvider(client);

    @Before
    public void setup() {
//        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.post(any())).thenReturn(response);
        setProperty(ARENA_ORDS_CLIENT_ID_PROPERTY, "client_id");
        setProperty(ARENA_ORDS_CLIENT_SECRET_PROPERTY, "client_secret");
        setProperty(ARENA_ORDS_URL_PROPERTY, "ords_url");
    }

    @Test
    public void henterGyldigTokenFraCache() {

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
        when(response.readEntity(OrdsToken.class)).thenReturn(ordsToken);
    }
}
