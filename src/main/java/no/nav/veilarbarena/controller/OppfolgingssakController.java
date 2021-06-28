package no.nav.veilarbarena.controller;

import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.service.OppfolgingssakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oppfolgingssak")
public class OppfolgingssakController {

    private final AuthService authService;

    private final OppfolgingssakService service;

    @Autowired
    public OppfolgingssakController(AuthService authService, OppfolgingssakService service) {
        this.authService = authService;
        this.service = service;
    }

    @GetMapping("/{fnr}")
    public OppfolgingssakDTO oppfolgingssak(@PathVariable("fnr") String fnr) {
        authService.sjekkTilgang(fnr);
        return service.hentOppfolginssak(fnr);
    }
}
