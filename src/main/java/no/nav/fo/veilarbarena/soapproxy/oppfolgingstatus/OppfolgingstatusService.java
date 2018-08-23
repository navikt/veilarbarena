package no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.fo.veilarbarena.scheduled.User;
import no.nav.fo.veilarbarena.scheduled.UserChangeListener;
import no.nav.fo.veilarbarena.scheduled.UserChangePublisher;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.binding.OppfoelgingsstatusV1;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.meldinger.HentOppfoelgingsstatusRequest;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.meldinger.HentOppfoelgingsstatusResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import static no.nav.fo.veilarbarena.DateUtils.xmlGregorianCalendarToLocalDate;

@Slf4j
public class OppfolgingstatusService implements UserChangeListener {

    @Inject
    private OppfoelgingsstatusV1 service;

    @Inject
    private UserChangePublisher userChangePublisher;

    @PostConstruct
    public void setup() {
        userChangePublisher.subscribe(this);
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
        log.debug("Invalidating user: {}", user);
    }

    private static Oppfolgingstatus toOppfolgingsstatus(HentOppfoelgingsstatusResponse response) {
        return Oppfolgingstatus.builder()
                .oppfolgingsEnhet(response.getNavOppfoelgingsenhet())
                .rettighetsgruppeKode(response.getRettighetsgruppeKode().getValue())
                .formidlingsgruppeKode(response.getFormidlingsgruppeKode().getValue())
                .serviceGruppeKode(response.getServicegruppeKode().getValue())
                .inaktiveringsdato(xmlGregorianCalendarToLocalDate(response.getInaktiveringsdato()))
                .harMottaOppgaveIArena(response.getHarOppgaveMottaSelvregPerson())
                .build();
    }
}
