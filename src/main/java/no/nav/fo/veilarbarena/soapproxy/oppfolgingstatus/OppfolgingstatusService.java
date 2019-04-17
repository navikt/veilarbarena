package no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.domain.User;
import no.nav.fo.veilarbarena.scheduled.UserChangeListener;

import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.binding.OppfoelgingsstatusV2;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.informasjon.Person;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.meldinger.HentOppfoelgingsstatusRequest;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v2.meldinger.HentOppfoelgingsstatusResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.utils.DateUtils.xmlGregorianCalendarToLocalDate;

@Slf4j
public class OppfolgingstatusService implements UserChangeListener {

    private final OppfoelgingsstatusV2 service;

    @Inject
    public OppfolgingstatusService(OppfoelgingsstatusV2 service) {
        this.service = service;
    }

    @Cacheable(cacheNames = OppfolgingstatusCache.NAME, key = "#fnr.get()")
    public Try<Oppfolgingstatus> hentOppfoelgingsstatus(PersonId.Fnr fnr) {
        return Try.of(() -> {
            HentOppfoelgingsstatusRequest request = new HentOppfoelgingsstatusRequest();

            Person person = new Person();
            person.setIdent(fnr.get());
            request.setBruker(person);

            return toOppfolgingsstatus(service.hentOppfoelgingsstatus(request));
        });
    }

    @Override
    @CacheEvict(cacheNames = OppfolgingstatusCache.NAME, key = "#user.fodselsnr")
    public void userChanged(User user) {
        log.debug("Invalidating user: {}", user.toString());
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
