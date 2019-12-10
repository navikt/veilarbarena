package no.nav.fo.veilarbarena.controller;

import no.nav.fo.veilarbarena.api.OppfolgingsstatusDTO;
import no.nav.fo.veilarbarena.service.AuthService;
import no.nav.fo.veilarbarena.service.OppfolgingsstatusService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Component
@Path("/oppfolgingsstatus/{fnr}")
public class OppfolgingsstatusController {

    @Inject
    private AuthService authService;

    @Inject
    private OppfolgingsstatusService service;

    @GET
    public OppfolgingsstatusDTO oppfolgingsstatus(@PathParam("fnr") String fnr) {
        authService.sjekkTilgang(fnr);
        return service.hentOppfolgingsstatus(fnr);
    }
}
