package no.nav.veilarbarena.soapproxy.oppfolgingstatus;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.domain.PersonId;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.HentOppfoelgingsstatusPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.HentOppfoelgingsstatusSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.HentOppfoelgingsstatusUgyldigInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;


@RestController
@RequestMapping("/api/oppfolgingstatus")
public class OppfolgingstatusController {

    private final OppfolgingstatusService service;

    private final AktoerRegisterService aktoerRegisterService;

    private final AuthService authService;

    @Autowired
    public OppfolgingstatusController(OppfolgingstatusService service, AktoerRegisterService aktoerRegisterService, AuthService authService) {
        this.service = service;
        this.aktoerRegisterService = aktoerRegisterService;
        this.authService = authService;
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "fnr", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "aktorId", dataType = "string", paramType = "query")
    })
    @GET
    public Oppfolgingstatus oppfolgingstatus() {
        PersonId.Fnr fnr = getUserIdent(requestProvider)
                .map((userid) -> userid.toFnr(aktoerRegisterService))
                .getOrElseThrow(() -> new BadRequestException("Missing userid"));

        authService.sjekkTilgang(fnr.get());
        return hentOppfolgingsstatus(fnr);
    }

    public static Option<PersonId> getUserIdent(Provider<HttpServletRequest> requestProvider) {
        Option<PersonId> fnr = Option.of(requestProvider.get().getParameter("fnr")).map(PersonId::fnr);
        Option<PersonId> aktorId = Option.of(requestProvider.get().getParameter("aktorId")).map(PersonId::fnr);

        return fnr.orElse(aktorId);
    }

    private Oppfolgingstatus hentOppfolgingsstatus(PersonId.Fnr userId) {
        return service.hentOppfoelgingsstatus(userId)
                .getOrElseThrow((error) -> Match(error).of(
                        Case($(instanceOf(HentOppfoelgingsstatusPersonIkkeFunnet.class)), (e) -> new NotFoundException("Could not find user: " + userId, e)),
                        Case($(instanceOf(HentOppfoelgingsstatusSikkerhetsbegrensning.class)), (e) -> new ForbiddenException("Cannot give you that user: " + userId, e)),
                        Case($(instanceOf(HentOppfoelgingsstatusUgyldigInput.class)), (e) -> new BadRequestException("Something went wrong when fetching: " + userId, e)),
                        Case($(), (e) -> new InternalServerErrorException("Unhandled exception", e)))
                );
    }
}
