package no.nav.fo.veilarbarena.controller;

import no.nav.fo.veilarbarena.api.OppfolgingssakDTO;
import no.nav.fo.veilarbarena.service.AuthService;
import no.nav.fo.veilarbarena.service.OppfolgingssakService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Component
@Path("/oppfolgingssak/{fnr}")
public class OppfolgingssakController {

    @Inject
    private AuthService authService;

    @Inject
    private OppfolgingssakService service;

    @GET
    public OppfolgingssakDTO oppfolgingssak(@PathParam("fnr") String fnr) {
        authService.sjekkTilgang(fnr);
        return service.hentOppfolginssak(fnr);
    }
}
