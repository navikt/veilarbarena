package no.nav.veilarbarena.client;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class ArenaOrdsClientImpl implements ArenaOrdsClient {

    private final String arenaOrdsUrl;
    private final Supplier<String> arenaOrdsTokenProvider;
    private final OkHttpClient client = RestClient.baseClient();

    public ArenaOrdsClientImpl(String arenaOrdsUrl, Supplier<String> arenaOrdsTokenProvider) {
        this.arenaOrdsUrl = arenaOrdsUrl;
        this.arenaOrdsTokenProvider = arenaOrdsTokenProvider;
    }

    @SneakyThrows
    public <T> T get(String path, String fnr, Class<T> clazz) {
        String url = joinPaths(arenaOrdsUrl, "arena/api/v1/person/oppfoelging", path) + "?p_fnr=" + fnr;

        Request request = new Request.Builder()
                .url(url)
                .header(AUTHORIZATION, "Bearer " + arenaOrdsTokenProvider.get())
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.parseJsonResponseOrThrow(response, clazz);
        }
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(arenaOrdsUrl, "arena/api/v1/test/ping"), client);
    }
}
