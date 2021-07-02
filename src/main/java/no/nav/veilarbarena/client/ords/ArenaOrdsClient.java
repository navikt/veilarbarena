package no.nav.veilarbarena.client.ords;

import no.nav.common.health.HealthCheck;

import java.util.Optional;

public interface ArenaOrdsClient extends HealthCheck {

    <T> Optional<T> get(String path, String fnr, Class<T> clazz);

}
