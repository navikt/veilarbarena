package no.nav.fo.veilarbarena.client;

import com.fasterxml.jackson.core.type.TypeReference;
import no.nav.fo.veilarbarena.domain.IdentinfoForAktoer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

import static no.nav.fo.veilarbarena.config.ApplicationConfig.AKTOERREGISTER_API_V1_URL;
import static no.nav.json.JsonUtils.fromJson;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Component
public class AktoerRegisterClient {
    private final String aktorRegisterUrl = getRequiredProperty(AKTOERREGISTER_API_V1_URL);
    private Client client;

    @Inject
    public AktoerRegisterClient(Client client) {
        this.client = client;
    }

    public String tilAktorId(String fnr) {
        return konverterId(fnr, "AktoerId");
    }

    public String tilFnr(String aktoerId) {
        return konverterId(aktoerId, "NorskIdent");
    }

    private String konverterId(String ident, String type) {
        String uri = UriComponentsBuilder.fromHttpUrl(aktorRegisterUrl)
                .path("/identer")
                .queryParam("identgruppe", type)
                .queryParam("gjeldende", true)
                .toUriString();

        Response resp = client
                .target(uri)
                .request()
                .headers(httpHeaders(ident))
                .get();

        if (resp.getStatus() >= 200 && resp.getStatus() < 300) {
            final IdentinfoForAktoer identinfoForAktoer = fromResponse(resp, ident);
            validerRespons(ident, identinfoForAktoer, type);
            return hentGjeldendeId(identinfoForAktoer);
        } else {
            return "";
        }
    }

    private IdentinfoForAktoer fromResponse(Response response, String ident) {
        String json = response.readEntity(String.class);
        TypeReference<Map<String, IdentinfoForAktoer>> type = new TypeReference<Map<String, IdentinfoForAktoer>>() {};

        return fromJson(json, type).get(ident);
    }

    private MultivaluedMap<String, Object> httpHeaders(String aktorId) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("Nav-Call-Id", UUID.randomUUID().toString());
        headers.putSingle("Nav-Consumer-Id", "srvveilarbarena");
        headers.putSingle("Nav-Personidenter", aktorId);
        return headers;

    }

    private String hentGjeldendeId(IdentinfoForAktoer identinfoForAktoer) {
        return identinfoForAktoer.getIdenter().get(0).getIdent();
    }

    private void validerRespons(String ident, IdentinfoForAktoer identinfoForAktoer, String type) {
        if (identinfoForAktoer.getIdenter() == null) {
            throw new RuntimeException("Fant ingen identinfo for id: " + ident + " type " + type);
        }

        if (identinfoForAktoer.getFeilmelding() != null) {
            throw new RuntimeException("Feil fra aktørregister for id " + ident + " type " + type + ", feilmelding: " + identinfoForAktoer.getFeilmelding());
        }

        if (identinfoForAktoer.getIdenter().size() != 1) {
            throw new RuntimeException("Forventet 1 fnr for id" + ident + " type " + type + ", fant " + identinfoForAktoer.getIdenter().size());
        }
    }
}
