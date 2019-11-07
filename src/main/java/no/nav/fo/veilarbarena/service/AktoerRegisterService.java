package no.nav.fo.veilarbarena.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.IdentinfoForAktoer;
import no.nav.fo.veilarbarena.domain.PersonId;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static no.nav.fo.veilarbarena.config.ApplicationConfig.AKTOERREGISTER_API_V1_URL;
import static no.nav.sbl.util.EnvironmentUtils.getRequiredProperty;

@Slf4j
@Component
public class AktoerRegisterService {
    private final String aktorRegisterUrl = getRequiredProperty(AKTOERREGISTER_API_V1_URL);
    private Client client;

    @Inject
    public AktoerRegisterService(Client client) {
        this.client = client;
    }

    public String tilAktorId(String fnr) {
        return konverterId(fnr, "AktoerId");
    }

    public Map<PersonId.Fnr, IdentinfoForAktoer> tilAktorIdList(List<String> fnr) {
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
                .headers(httpHeaders(singletonList(ident)))
                .get();

        if (resp.getStatus() >= 200 && resp.getStatus() < 300) {
            final IdentinfoForAktoer identinfoForAktoer = fromResponse(resp, ident);
            validerRespons(ident, identinfoForAktoer, type);
            return hentGjeldendeId(identinfoForAktoer);
        } else {
            return "";
        }
    }

    private Map<PersonId.Fnr, IdentinfoForAktoer> konverterId(List<String> identer, String type) {
        String uri = UriComponentsBuilder.fromHttpUrl(aktorRegisterUrl)
                .path("/identer")
                .queryParam("identgruppe", type)
                .queryParam("gjeldende", true)
                .toUriString();

        Response resp = client
                .target(uri)
                .request()
                .headers(httpHeaders(identer))
                .get();

        if (resp.getStatus() >= 200 && resp.getStatus() < 300) {
            final Map<PersonId.Fnr, IdentinfoForAktoer> identinfoForAktoerMap = fromResponse(resp);
            logFeilsituasjoner(identinfoForAktoerMap);
            return identinfoForAktoerMap;
        } else {
            return emptyMap();
        }
    }

    private IdentinfoForAktoer fromResponse(Response response, String ident) {
        return response.readEntity(new GenericType<Map<String, IdentinfoForAktoer>>(){})
                .get(ident);
    }

    private Map<PersonId.Fnr, IdentinfoForAktoer> fromResponse(Response response) {
        return response.readEntity(new GenericType<Map<PersonId.Fnr, IdentinfoForAktoer>>(){});
    }

    private MultivaluedMap<String, Object> httpHeaders(List<String> aktorId) {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle("Nav-Call-Id", UUID.randomUUID().toString());
        headers.putSingle("Nav-Consumer-Id", "srvveilarbarena");
        headers.putSingle("Nav-Personidenter", aktorId.toString().replace("[", "").replace("]", ""));
        return headers;

    }

    private String hentGjeldendeId(IdentinfoForAktoer identinfoForAktoer) {
        return identinfoForAktoer.getIdenter().get(0).getIdent();
    }

    private void logFeilsituasjoner(Map<PersonId.Fnr, IdentinfoForAktoer> identinfoForAktoer) {
        List<String> feilende = new ArrayList<>();
        identinfoForAktoer.forEach((fnr, identinfo) -> {
            if (identinfo.getFeilmelding() != null) {
                feilende.add(fnr.get());
            }
        });

        if (!feilende.isEmpty()) {
            log.warn("Fant ikke aktør-id for {} antall fødselsnummer", feilende.size());
        }
    }

        private void validerRespons(String ident, IdentinfoForAktoer identinfoForAktoer, String type) {
        if (identinfoForAktoer.getIdenter() == null) {
            throw new IllegalArgumentException("Fant ingen identinfo for id: " + ident + " type " + type);
        }

        if (identinfoForAktoer.getFeilmelding() != null) {
            throw new IllegalArgumentException("Feil fra aktørregister for id " + ident + " type " + type + ", feilmelding: " + identinfoForAktoer.getFeilmelding());
        }

        if (identinfoForAktoer.getIdenter().size() != 1) {
            throw new IllegalArgumentException("Forventet 1 fnr for id" + ident + " type " + type + ", fant " + identinfoForAktoer.getIdenter().size());
        }
    }
}
