package no.nav.veilarbarena.utils;

import no.nav.common.types.identer.Fnr;
import org.json.JSONObject;

public class FnrMaker {
    public static Fnr hentFnr(String fnr) {
        JSONObject request = new JSONObject(fnr);
        return Fnr.of(request.getString(fnr));
    }
}
