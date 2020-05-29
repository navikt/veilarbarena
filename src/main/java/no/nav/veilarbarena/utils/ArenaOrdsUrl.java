package no.nav.veilarbarena.utils;

import static no.nav.common.utils.EnvironmentUtils.getOptionalProperty;
import static no.nav.common.utils.UrlUtils.clusterUrlForApplication;
import static no.nav.common.utils.UrlUtils.joinPaths;

public class ArenaOrdsUrl {
    public static final String ARENA_ORDS_URL_PROPERTY = "ARENA_ORDS_URL";

    public static String get(String path) {
        return joinPaths(getOptionalProperty(ARENA_ORDS_URL_PROPERTY).orElseGet(() -> clusterUrlForApplication("arena-ords")), path);
    }
}
