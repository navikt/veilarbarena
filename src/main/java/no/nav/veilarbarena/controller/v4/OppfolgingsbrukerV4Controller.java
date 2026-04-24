package no.nav.veilarbarena.controller.v4;

import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.client.ords.dto.PersonRequest;
import no.nav.veilarbarena.controller.response.OppfolgingsbrukerV3DTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/v4")
public class OppfolgingsbrukerV4Controller {

    private final ArenaService arenaService;
    private final AuthService authService;

    @Autowired
    public OppfolgingsbrukerV4Controller(ArenaService arenaService, AuthService authService) {
        this.arenaService = arenaService;
        this.authService = authService;
    }

    @PostMapping("/hent-oppfolgingsbruker")
    public OppfolgingsbrukerV3DTO getOppfolgingsbrukerV4(@RequestBody PersonRequest personRequest) {
        authService.sjekkTilgang(personRequest.getFnr());

        return arenaService.hentOppfolgingsbruker(personRequest.getFnr())
                .map(OppfolgingsbrukerV3DTO::fraOppfolgingsbruker)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }
}
