package no.nav.fo.veilarbarena.utils;

import static no.nav.apiapp.util.UrlUtils.clusterUrlForApplication;
import static no.nav.apiapp.util.UrlUtils.joinPaths;
import static no.nav.sbl.util.EnvironmentUtils.getOptionalProperty;

public class ArenaOrdsUrl {
    public static final String ARENA_ORDS_URL_PROPERTY = "ARENA_ORDS_URL";

    public static String get(String path) {
        return joinPaths(getOptionalProperty(ARENA_ORDS_URL_PROPERTY)
                        .orElseGet(() -> clusterUrlForApplication("arena-ords")), path);
    }
}
