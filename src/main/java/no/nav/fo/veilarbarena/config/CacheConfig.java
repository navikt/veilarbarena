package no.nav.fo.veilarbarena.config;

import io.vavr.collection.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
import no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus.OppfolgingstatusCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static no.nav.dialogarena.aktor.AktorConfig.AKTOR_ID_FROM_FNR_CACHE;
import static no.nav.dialogarena.aktor.AktorConfig.FNR_FROM_AKTOR_ID_CACHE;

@Configuration
@EnableCaching
public class CacheConfig {
    public static CacheConfiguration setupCache(String name, int maxEntriesLocalHeap, long timeToIdleSeconds, long timeToLiveSeconds) {
        return new CacheConfiguration(name, maxEntriesLocalHeap)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU)
                .timeToIdleSeconds(timeToIdleSeconds)
                .timeToLiveSeconds(timeToLiveSeconds)
                .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.LOCALTEMPSWAP));
    }

    private static void makeBlocking(net.sf.ehcache.CacheManager manager, CacheConfiguration... cacheNames) {
        List.of(cacheNames)
                .forEach((cacheName) -> {
                    Cache cache = manager.getCache(cacheName.getName());
                    BlockingCache blockingCache = new BlockingCache(cache);
                    blockingCache.setTimeoutMillis(10000);
                    manager.replaceCacheWithDecoratedCache(cache, blockingCache);
                });
    }

    @Bean
    public CacheManager cacheManager() {
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(AKTOR_ID_FROM_FNR_CACHE);
        config.addCache(FNR_FROM_AKTOR_ID_CACHE);
        config.addCache(OppfolgingstatusCache.CONFIG);

        net.sf.ehcache.CacheManager manager = net.sf.ehcache.CacheManager.newInstance(config);
        makeBlocking(manager, OppfolgingstatusCache.CONFIG);

        return new EhCacheCacheManager(manager);
    }
}
