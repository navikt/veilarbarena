package no.nav.veilarbarena.client.ords;

import lombok.SneakyThrows;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Optional;
import java.util.function.Supplier;

import static no.nav.common.json.JsonUtils.fromJson;
import static no.nav.common.utils.UrlUtils.joinPaths;
import static no.nav.veilarbarena.utils.XmlUtils.fromXml;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class ArenaOrdsClientImpl implements ArenaOrdsClient {

    private final String arenaOrdsUrl;
    private final Supplier<String> arenaOrdsTokenProvider;
    private final OkHttpClient client = RestClient.baseClient();

    public ArenaOrdsClientImpl(String arenaOrdsUrl, Supplier<String> arenaOrdsTokenProvider) {
        this.arenaOrdsUrl = arenaOrdsUrl;
        this.arenaOrdsTokenProvider = arenaOrdsTokenProvider;
    }

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckUtils.pingUrl(joinPaths(arenaOrdsUrl, "arena/api/v1/test/ping"), client);
    }

    @Override
    public Optional<ArenaOppfolgingsstatusDTO> hentArenaOppfolgingsstatus(Fnr fnr) {
        String url = joinPaths(arenaOrdsUrl, "arena/api/v2/person/oppfoelging/oppfoelgingsstatus");
        return get(url, fnr)
                .map(body -> fromJson(body, ArenaOppfolgingsstatusDTO.class));
    }

    @Override
    public Optional<ArenaOppfolgingssakDTO> hentArenaOppfolgingssak(Fnr fnr) {
        String url = joinPaths(arenaOrdsUrl, "arena/api/v2/person/oppfoelging/oppfoelgingssak");
        return get(url, fnr)
                .map(body -> fromJson(body, ArenaOppfolgingssakDTO.class));
    }

    @SneakyThrows
    @Override
    public Optional<ArenaAktiviteterDTO> hentArenaAktiviteter(Fnr fnr) {
        String url = joinPaths(arenaOrdsUrl, "arena/api/v1/person/oppfoelging/aktiviteter");

        Request request = new Request.Builder()
                .url(url)
                .header("fnr", fnr.get())
                .header(AUTHORIZATION, RestUtils.createBearerToken(arenaOrdsTokenProvider.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.getBodyStr(response)
                    .map(body -> fromXml(body, ArenaAktiviteterDTO.class));
        }
    }

    @SneakyThrows
    private Optional<String> get(String path, Fnr fnr) {
        Request request = new Request.Builder()
                .url(path)
                .header("fnr", fnr.get())
                .header(AUTHORIZATION, RestUtils.createBearerToken(arenaOrdsTokenProvider.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);
            return RestUtils.getBodyStr(response);
        }
    }

}
