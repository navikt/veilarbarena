package no.nav.veilarbarena.mock;

import no.nav.common.client.aktoroppslag.AktorOppslagClient;
import no.nav.common.client.aktoroppslag.BrukerIdenter;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.common.health.HealthCheckResult;
import no.nav.common.types.identer.AktorId;
import no.nav.common.types.identer.EksternBrukerId;
import no.nav.common.types.identer.Fnr;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static no.nav.veilarbarena.utils.TestData.TEST_AKTOR_ID;
import static no.nav.veilarbarena.utils.TestData.TEST_FNR;

public class AktorregisterClientMock implements AktorOppslagClient {

    @Override
    public HealthCheckResult checkHealth() {
        return HealthCheckResult.healthy();
    }

    @Override
    public Fnr hentFnr(AktorId aktorId) {
        return Fnr.of(TEST_FNR);
    }

    @Override
    public AktorId hentAktorId(Fnr fnr) {
        return AktorId.of(TEST_AKTOR_ID);
    }

    @Override
    public Map<AktorId, Fnr> hentFnrBolk(List<AktorId> aktorIdListe) {
        return Collections.emptyMap();
    }

    @Override
    public Map<Fnr, AktorId> hentAktorIdBolk(List<Fnr> fnrListe) {
        return Collections.emptyMap();
    }

    @Override
    public BrukerIdenter hentIdenter(EksternBrukerId brukerId) {
        return null;
    }

}
