package no.nav.veilarbarena.controller.v2;

import no.nav.veilarbarena.client.ords.dto.PersonRequest;
import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v2/oppfolgingsstatus/hent")
public class OppfolgingsstatusV2Controller {

    private final AuthService authService;

    private final ArenaService arenaService;

    @Autowired
    public OppfolgingsstatusV2Controller(AuthService authService, ArenaService arenaService) {
        this.authService = authService;
        this.arenaService = arenaService;
    }

    @PostMapping
    public OppfolgingsstatusDTO hentArenaOppfolgingsstatusV2(@RequestBody PersonRequest personRequest) {
        authService.sjekkTilgang(personRequest.getFnr());
        return arenaService.hentArenaOppfolgingsstatus(personRequest.getFnr())
                .map(DtoMapper::mapTilOppfolgingsstatusDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
