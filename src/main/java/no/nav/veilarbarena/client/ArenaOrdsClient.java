package no.nav.veilarbarena.client;

import no.nav.common.health.HealthCheck;

public interface ArenaOrdsClient extends HealthCheck {

    <T> T get(String path, String fnr, Class<T> clazz);

}
