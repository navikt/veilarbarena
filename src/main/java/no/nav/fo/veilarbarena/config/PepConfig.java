package no.nav.fo.veilarbarena.config;

import no.nav.apiapp.security.veilarbabac.VeilarbAbacPepClient;
import no.nav.brukerdialog.security.oidc.SystemUserTokenProvider;
import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;
import no.nav.sbl.featuretoggle.unleash.UnleashService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.inject.Inject;

@Configuration
@Import({AbacContext.class, UnleashConfig.class})
public class PepConfig {

    private SystemUserTokenProvider systemUserTokenProvider = new SystemUserTokenProvider();

    @Inject
    private UnleashService unleashService;

    @Bean
    public VeilarbAbacPepClient pepClient(Pep pep) {
        VeilarbAbacPepClient.Builder builder = VeilarbAbacPepClient.ny()
                .medPep(pep)
                .medResourceTypePerson()
                .medSystemUserTokenProvider(() -> systemUserTokenProvider.getToken())
                .brukAktoerId(() -> unleashService.isEnabled("veilarboppfolging.veilarbabac.aktor"))
                .sammenlikneTilgang(() -> unleashService.isEnabled("veilarboppfolging.veilarbabac.sammenlikn"))
                .foretrekkVeilarbAbacResultat(() -> unleashService.isEnabled("veilarboppfolging.veilarbabac.foretrekk_veilarbabac"));

        return builder.bygg();
    }

}
