package no.nav.fo.veilarbarena.service;

import lombok.SneakyThrows;
import no.nav.apiapp.feil.Feil;
import no.nav.apiapp.selftest.Helsesjekk;
import no.nav.apiapp.selftest.HelsesjekkMetadata;
import no.nav.fo.veilarbarena.utils.ArenaOrdsTokenProvider;
import no.nav.fo.veilarbarena.utils.ArenaOrdsUrl;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import no.nav.sbl.rest.RestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static no.nav.apiapp.feil.FeilType.FINNES_IKKE;

public class ArenaOrdsService implements Helsesjekk {

    private final ArenaOrdsTokenProvider arenaOrdsTokenProvider;
    private final UnleashService unleashService;
    private final Client client = RestUtils.createClient();

    public ArenaOrdsService(ArenaOrdsTokenProvider arenaOrdsTokenProvider, UnleashService unleashService) {
        this.arenaOrdsTokenProvider = arenaOrdsTokenProvider;
        this.unleashService = unleashService;
    }

    @SneakyThrows
    public <T> T get(String path, String fnr, Class<T> clazz) {
        if (unleashService.isEnabled("veilarbarena.arena_ords")) {
            Response response = client
                    .target(ArenaOrdsUrl.get("arena/api/v1/person/oppfoelging"))
                    .path(path)
                    .queryParam("p_fnr", fnr)
                    .request()
                    .header(AUTHORIZATION, "Bearer " + arenaOrdsTokenProvider.getToken())
                    .get();

            return response.readEntity(clazz);
        } else {
            throw new Feil(FINNES_IKKE);
        }
    }

    @Override
    public void helsesjekk() {
        if (unleashService.isEnabled("veilarbarena.arena_ords")) {
            int status = client
                    .target(ArenaOrdsUrl.get("arena/api/v1/test/ping"))
                    .request()
                    .get()
                    .getStatus();
            if (status != 200) {
                throw new IllegalStateException("Feil i selftest mot Arena ORDS - status = " + status);
            }
        }
    }

    @Override
    public HelsesjekkMetadata getMetadata() {
        return new HelsesjekkMetadata("arena_ords_ping", ArenaOrdsUrl.get("arena/api/v1/test/ping"), "Arena ORDS - ping", false);
    }
}
