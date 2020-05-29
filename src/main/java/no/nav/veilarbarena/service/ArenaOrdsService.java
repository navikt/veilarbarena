package no.nav.veilarbarena.service;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.veilarbarena.utils.ArenaOrdsTokenProvider;
import no.nav.veilarbarena.utils.ArenaOrdsUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.common.utils.UrlUtils.joinPaths;

@Service
public class ArenaOrdsService implements HealthCheck {

    private final ArenaOrdsTokenProvider arenaOrdsTokenProvider;
    private final OkHttpClient client = RestClient.baseClient();

    @Autowired
    public ArenaOrdsService(ArenaOrdsTokenProvider arenaOrdsTokenProvider) {
        this.arenaOrdsTokenProvider = arenaOrdsTokenProvider;
    }

    @SneakyThrows
    public <T> T get(String path, String fnr, Class<T> clazz) {
        String url = joinPaths(ArenaOrdsUrl.get("arena/api/v1/person/oppfoelging"), path) + "?p_fnr=" + fnr;

        Request request = new Request.Builder()
                .url(url)
                .header(AUTHORIZATION, "Bearer " + arenaOrdsTokenProvider.getToken())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseBodyOrThrow(response.body(), clazz);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(ArenaOrdsUrl.get("arena/api/v1/test/ping"), client);
    }
}
