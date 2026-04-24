package no.nav.veilarbarena.controller.v3;

import no.nav.veilarbarena.client.ords.dto.PersonRequest;
import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v3")
public class OppfolgingsstatusV3Controller {

    private final AuthService authService;

    private final ArenaService arenaService;

    @Autowired
    public OppfolgingsstatusV3Controller(AuthService authService, ArenaService arenaService) {
        this.authService = authService;
        this.arenaService = arenaService;
    }

    @PostMapping("/hent-oppfolgingsstatus")
    public OppfolgingsstatusDTO hentArenaOppfolgingsstatusV3(@RequestBody PersonRequest personRequest) {
        authService.sjekkTilgang(personRequest.getFnr());
        return arenaService.hentArenaOppfolgingsstatus(personRequest.getFnr())
                .map(DtoMapper::mapTilOppfolgingsstatusDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }
}
