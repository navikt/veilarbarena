package no.nav.fo.veilarbarena.service;

import lombok.SneakyThrows;
import no.nav.fo.veilarbarena.utils.ArenaOrdsTokenProvider;
import no.nav.fo.veilarbarena.utils.ArenaOrdsUrl;
import no.nav.sbl.rest.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class ArenaOrdsService {

    private final ArenaOrdsTokenProvider arenaOrdsTokenProvider;
    private final Client client = RestUtils.createClient();

    public ArenaOrdsService(ArenaOrdsTokenProvider arenaOrdsTokenProvider) {
        this.arenaOrdsTokenProvider = arenaOrdsTokenProvider;
    }

    @SneakyThrows
    public <T> T get(String path, String fnr, Class<T> clazz) {
        Response response = client
                .target(ArenaOrdsUrl.get("arena/api/v1/person/oppfoelging"))
                .path(path)
                .queryParam("p_fnr", fnr)
                .request()
                .header(AUTHORIZATION, "Bearer " + arenaOrdsTokenProvider.getToken())
                .get();

        return response.readEntity(clazz);
    }
}
