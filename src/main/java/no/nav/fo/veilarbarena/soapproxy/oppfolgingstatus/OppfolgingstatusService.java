package no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus;

import io.vavr.control.Try;
import no.nav.fo.veilarbarena.domain.PersonId;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.binding.OppfoelgingsstatusV1;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.meldinger.HentOppfoelgingsstatusRequest;
import no.nav.tjeneste.virksomhet.oppfoelgingsstatus.v1.meldinger.HentOppfoelgingsstatusResponse;
import org.springframework.cache.annotation.Cacheable;

import javax.inject.Inject;

import static no.nav.fo.veilarbarena.DateUtils.xmlGregorianCalendarToLocalDate;

public class OppfolgingstatusService {

    @Inject
    private OppfoelgingsstatusV1 service;

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
