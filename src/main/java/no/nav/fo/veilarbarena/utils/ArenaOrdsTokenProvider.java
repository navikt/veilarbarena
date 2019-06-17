package no.nav.fo.veilarbarena.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Value;
import no.nav.sbl.rest.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Base64;

import static java.time.temporal.ChronoUnit.SECONDS;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CACHE_CONTROL;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED_TYPE;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

public class ArenaOrdsTokenProvider {

    public static final String ARENA_ORDS_CLIENT_ID_PROPERTY = "ARENA_ORDS_CLIENT_ID";
    public static final String ARENA_ORDS_CLIENT_SECRET_PROPERTY = "ARENA_ORDS_CLIENT_SECRET";

    private static final int MINIMUM_TIME_TO_EXPIRE_BEFORE_REFRESH = 60;
    private final Client client;

    public ArenaOrdsTokenProvider() {
        this(RestUtils.createClient());
    }

    public ArenaOrdsTokenProvider(Client client) {
        this.client = client;
    }

    @Getter
    private class TokenCache {
        private final OrdsToken ordsToken;
        private final LocalDateTime time;

        TokenCache(OrdsToken ordsToken) {
            this.ordsToken = ordsToken;
            this.time = LocalDateTime.now();
        }
    }

    private TokenCache tokenCache = null;

    public String getToken() {
        if (tokenIsSoonExpired()) {
            refreshToken();
        }
        return tokenCache.getOrdsToken().getAccessToken();
    }

    private void refreshToken() {

        Response response = client
                .target(ArenaOrdsUrl.get("arena/api/oauth/token"))
                .request()
                .header(AUTHORIZATION, basicCredentials(
                        getRequiredProperty(ARENA_ORDS_CLIENT_ID_PROPERTY),
                        getRequiredProperty(ARENA_ORDS_CLIENT_SECRET_PROPERTY)))
                .header(CACHE_CONTROL, "no-cache")
                .post(Entity.entity("grant_type=client_credentials", APPLICATION_FORM_URLENCODED_TYPE));

        OrdsToken ordsToken = response.readEntity(OrdsToken.class);

        this.tokenCache = new TokenCache(ordsToken);
    }

    private static String basicCredentials(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
    }

    private boolean tokenIsSoonExpired() {
        return
                tokenCache == null ||
                        timeToRefresh().isBefore(LocalDateTime.now());
    }

    private LocalDateTime timeToRefresh() {
        return tokenCache.getTime().plus(
                tokenCache.getOrdsToken().getExpiresIn() - MINIMUM_TIME_TO_EXPIRE_BEFORE_REFRESH, SECONDS);
    }
}

@Value
class OrdsToken {
    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("token_type")
    String tokenType;
    @JsonProperty("expires_in")
    int expiresIn;
}
