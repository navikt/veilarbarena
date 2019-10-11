package no.nav.fo.veilarbarena.config;

import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AbacContext.class})
public class PepConfig {

    private SystemUserTokenProvider systemUserTokenProvider = new SystemUserTokenProvider();

    @Bean
    public VeilarbAbacPepClient pepClient(Pep pep) {
        VeilarbAbacPepClient.Builder builder = VeilarbAbacPepClient.ny()
                .medPep(pep)
                .medSystemUserTokenProvider(() -> systemUserTokenProvider.getToken())
                .brukAktoerId(() -> true)
                .sammenlikneTilgang(() -> false)
                .foretrekkVeilarbAbacResultat(() -> true);

        return builder.bygg();
    }

}
