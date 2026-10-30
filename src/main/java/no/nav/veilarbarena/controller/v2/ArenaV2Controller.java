package no.nav.veilarbarena.controller.v2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.client.ords.dto.PersonRequest;
import no.nav.veilarbarena.client.ords.dto.RegistrerIkkeArbeidssokerDto;
import no.nav.veilarbarena.config.EnvironmentProperties;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.service.PubliserOppfolgingsbrukerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/arena")
@Slf4j
public class ArenaV2Controller {

    private final AuthService authService;
    private final ArenaService arenaService;
    private final EnvironmentProperties environmentProperties;
    private final PubliserOppfolgingsbrukerService publiserOppfolgingsbrukerService;


    @PostMapping("/hent-status")
    public ArenaStatusDTO hentStatusV2(@RequestBody PersonRequest personRequest, @RequestHeader(value = "forceSync", required = false) boolean forceSync) {
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(personRequest.getFnr());
        } else {
            // TODO: Dette er en dårlig måte og sjekke tilganger på, bruk heller sjekk på access_as_application
            authService.sjekkAtSystembrukerErWhitelistet(
                    environmentProperties.getAmtTiltakClientId(),
                    environmentProperties.getAmtPersonServiceClientId(),
                    environmentProperties.getTiltaksgjennomforingApiClientId(),
                    environmentProperties.getVeilarbregistreringClientId(),
                    environmentProperties.getVeilarbregistreringClientIdGCP(),
                    environmentProperties.getPoaoTilgangGCPClientId(),
                    environmentProperties.getPoaoTilgangFSSClientId(),
                    environmentProperties.getAapOppgaveClientId(),
                    environmentProperties.getAapPostmottakClientId()
            );
        }

        return arenaService.hentArenaStatus(personRequest.getFnr(), forceSync)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Registrer ikke-arbeidssøker i arena
     */
    @PostMapping("/registrer-i-arena")
    public ResponseEntity<RegistrerIkkeArbeidssokerDto> registrerIkkeArbeidssoker(@RequestBody PersonRequest personRequest) {

        authService.sjekkTilgangKjerneregler(personRequest.getFnr());
        RegistrerIkkeArbeidssokerDto registrert = arenaService.registrerIkkeArbeidssoker(personRequest.getFnr())
                .orElse(RegistrerIkkeArbeidssokerDto.errorResult("Bruker ikke registrert"));
        /*
        OK_REGISTRERT_I_ARENA,
        FNR_FINNES_IKKE,
        KAN_REAKTIVERES_FORENKLET,
        BRUKER_ALLEREDE_ARBS,
        BRUKER_ALLEREDE_IARBS,
        UKJENT_FEIL
         */
        return switch (registrert.getKode()) {
            case OK_REGISTRERT_I_ARENA, BRUKER_ALLEREDE_ARBS, BRUKER_ALLEREDE_IARBS -> {
                    try {
                        arenaService.refreshMaterializedOppfolgingsBrukerView();
                        log.info("Refeshet materialized view for oppfolgingsbruker");
                        var blePublisert = publiserOppfolgingsbrukerService.publiserOppfolgingsbruker(personRequest.getFnr().get());
                        if (!blePublisert) {
                            log.warn("Fant ingen oppfølgingsbruker å publisere på kafka");
                        }
                    } catch (Exception e) {
                        /* Skal fortsatt svare med OK, endringer vil propagert via OppfolgingsbrukerEndretSchedule istedet */
                        log.warn("Kunne ikke publisere nylig registrert oppfolgingsbruker på kafka", e);
                    }
                    yield new ResponseEntity<>(registrert, HttpStatus.OK);
            }
            case UKJENT_FEIL, FNR_FINNES_IKKE, KAN_REAKTIVERES_FORENKLET -> {
                log.warn("Kunne ikke registrere bruker i arena: {}", registrert.getResultat());
                yield new ResponseEntity<>(registrert, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        };
    }

}
