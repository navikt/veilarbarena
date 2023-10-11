package no.nav.veilarbarena.utils;

import no.nav.common.types.identer.Fnr;

public class FnrMaker {
    public static Fnr hentFnr(String fnr) {
        return Fnr.of(fnr);
    }
}
