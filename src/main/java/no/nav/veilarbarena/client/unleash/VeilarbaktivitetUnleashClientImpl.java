package no.nav.veilarbarena.client.unleash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.common.json.JsonUtils;
import no.nav.common.rest.client.RestClient;
import no.nav.common.rest.client.RestUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static no.nav.common.utils.UrlUtils.joinPaths;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


public class VeilarbaktivitetUnleashClientImpl implements VeilarbaktivitetUnleashClient {

    private final OkHttpClient client = RestClient.baseClient();
    final String veilarbaktivitetUrl;
    final Supplier<String> tokenProvider;

    public static final String FEATURE_TOGGLE_NAME = "veilarbarena.oppfolgingsbrukerBatchDisabled";

    public VeilarbaktivitetUnleashClientImpl(String veilarbaktivitetUrl, Supplier<String> tokenProvider) {
        this.veilarbaktivitetUrl = veilarbaktivitetUrl;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Optional<Boolean> oppfolgingsbrukerBatchIsDisabled() {
        String url = joinPaths(veilarbaktivitetUrl, String.format("veilarbaktivitet/api/feature?feature=%s", FEATURE_TOGGLE_NAME));

        Request request = new Request.Builder()
                .url(url)
                .header(AUTHORIZATION, RestUtils.createBearerToken(tokenProvider.get()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            RestUtils.throwIfNotSuccessful(response);

            return RestUtils.getBodyStr(response)
                    .flatMap(stringBody -> {
                        TypeReference<HashMap<String, Boolean>> typeRef = new TypeReference<>() {};
                        try {
                            Map<String, Boolean> map = JsonUtils.getMapper().readValue(stringBody, typeRef);
                            return Optional.ofNullable(map.get(FEATURE_TOGGLE_NAME));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
