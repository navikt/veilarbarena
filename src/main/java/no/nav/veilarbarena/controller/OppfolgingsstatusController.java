package no.nav.veilarbarena.controller;

import no.nav.veilarbarena.domain.api.OppfolgingsstatusDTO;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.service.OppfolgingsstatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oppfolgingsstatus")
public class OppfolgingsstatusController {

    private final AuthService authService;

    private final OppfolgingsstatusService oppfolgingsstatusService;

    @Autowired
    public OppfolgingsstatusController(AuthService authService, OppfolgingsstatusService oppfolgingsstatusService) {
        this.authService = authService;
        this.oppfolgingsstatusService = oppfolgingsstatusService;
    }

    @GetMapping("/{fnr}")
    public OppfolgingsstatusDTO oppfolgingsstatus(@PathVariable("fnr") String fnr) {
        authService.sjekkTilgang(fnr);
        return oppfolgingsstatusService.hentOppfolgingsstatus(fnr);
    }
}
