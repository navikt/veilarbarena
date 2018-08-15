package no.nav.fo.veilarbarena;

import no.nav.sbl.dialogarena.types.Pingable;

public class ServiceUtils {
    public static Pingable createPingable(Runnable ping, Pingable.Ping.PingMetadata metadata) {
        return () -> {
            try {
                ping.run();
                return Pingable.Ping.lyktes(metadata);
            } catch (Exception e) {
                return Pingable.Ping.feilet(metadata, e);
            }
        };
    }
}
