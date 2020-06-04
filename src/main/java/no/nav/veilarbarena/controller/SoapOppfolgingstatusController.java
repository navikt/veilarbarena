package no.nav.veilarbarena.controller;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import no.nav.common.client.aktorregister.AktorregisterClient;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.domain.Oppfolgingstatus;
import no.nav.veilarbarena.service.SoapOppfolgingstatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.*;


@RestController
@RequestMapping("/api/oppfolgingstatus")
public class SoapOppfolgingstatusController {

    private final SoapOppfolgingstatusService service;

    private final AktorregisterClient aktorregisterClient;

    private final AuthService authService;

    @Autowired
    public SoapOppfolgingstatusController(SoapOppfolgingstatusService service, AktorregisterClient aktorregisterClient, AuthService authService) {
        this.service = service;
        this.aktorregisterClient = aktorregisterClient;
        this.authService = authService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "fnr", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "aktorId", dataType = "string", paramType = "query")
    })
    @GetMapping
    public Oppfolgingstatus oppfolgingstatus(@RequestParam(required = false) String fnr, @RequestParam(required = false) String aktorId) {

        if (fnr == null && aktorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mangler query param fnr/aktorId");
        } else if (fnr == null) {
            fnr = aktorregisterClient.hentFnr(aktorId);
        }

        authService.sjekkTilgang(fnr);
        return service.hentOppfoelgingsstatus(fnr);
    }

}
