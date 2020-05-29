package no.nav.veilarbarena.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.HentOppfoelgingsstatusPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.HentOppfoelgingsstatusSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.HentOppfoelgingsstatusUgyldigInput;
import no.nav.veilarbarena.domain.Oppfolgingstatus;
import no.nav.veilarbarena.domain.User;
import no.nav.veilarbarena.scheduled.UserChangeListener;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.meldinger.HentOppfoelgingsstatusRequest;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.meldinger.HentOppfoelgingsstatusResponse;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.OppfoelgingsstatusV2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

import static no.nav.veilarbarena.utils.DateUtils.xmlGregorianCalendarToLocalDate;

@Slf4j
public class SoapOppfolgingstatusService implements UserChangeListener {

    private final OppfoelgingsstatusV2 service;

    private final Cache<String, Oppfolgingstatus> cache;

    @Autowired
    public SoapOppfolgingstatusService(OppfoelgingsstatusV2 service) {
        this.service = service;
        cache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.DAYS)
                .maximumSize(30_000)
                .build();
    }

    public Oppfolgingstatus hentOppfoelgingsstatus(String fnr) {
        HentOppfoelgingsstatusRequest request = new HentOppfoelgingsstatusRequest();
        Person person = new Person();
        person.setIdent(fnr);
        request.setBruker(person);

        try {
            return toOppfolgingsstatus(service.hentOppfoelgingsstatus(request));
        } catch (Exception e) {
            log.error("Unable to find user", e);

            if (e instanceof HentOppfoelgingsstatusPersonIkkeFunnet) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find the user");
            } else if (e instanceof HentOppfoelgingsstatusSikkerhetsbegrensning) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to see this user");
            } else if (e instanceof HentOppfoelgingsstatusUgyldigInput) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Something went wrong when fetching");
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
            }
        }
    }

    @Override
    public void userChanged(User user) {
        log.debug("Invalidating user: {}", user.toString());
        cache.invalidate(user.getFodselsnr());
    }

    private static Oppfolgingstatus toOppfolgingsstatus(HentOppfoelgingsstatusResponse response) {
        return Oppfolgingstatus.builder()
                .oppfolgingsEnhet(response.getNavOppfoelgingsenhet())
                .rettighetsgruppeKode(response.getRettighetsgruppeKode().getValue())
                .formidlingsgruppeKode(response.getFormidlingsgruppeKode().getValue())
                .serviceGruppeKode(response.getServicegruppeKode().getValue())
                .inaktiveringsdato(xmlGregorianCalendarToLocalDate(response.getInaktiveringsdato()))
                .harMottaOppgaveIArena(response.isHarOppgaveMottaSelvregPerson())
                .build();
    }
}
