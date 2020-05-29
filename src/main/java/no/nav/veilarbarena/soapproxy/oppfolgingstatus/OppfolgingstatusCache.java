package no.nav.veilarbarena.soapproxy.oppfolgingstatus;

import net.sf.ehcache.config.CacheConfiguration;

public class OppfolgingstatusCache {
    public static final String NAME = "OPPFOELGINGSTATUS_CACHE";
    public static CacheConfiguration CONFIG = CacheConfig.setupCache(
            NAME,
            30000,
            7200,
            7200
    );
}
