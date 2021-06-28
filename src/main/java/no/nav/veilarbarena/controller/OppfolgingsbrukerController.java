package no.nav.veilarbarena.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerDTO;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/oppfolgingsbruker")
public class OppfolgingsbrukerController {

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository;

    private final AuthService authService;

    @Autowired
    public OppfolgingsbrukerController(OppfolgingsbrukerRepository oppfolgingsbrukerRepository, AuthService authService) {
        this.oppfolgingsbrukerRepository = oppfolgingsbrukerRepository;
        this.authService = authService;
    }

    @GetMapping("/{fnr}")
    public OppfolgingsbrukerDTO getOppfolgingsbruker(@PathVariable("fnr") String fnr) {
        authService.sjekkTilgang(fnr);

        return oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr)
                .map(OppfolgingsbrukerDTO::fraOppfolgingsbruker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
