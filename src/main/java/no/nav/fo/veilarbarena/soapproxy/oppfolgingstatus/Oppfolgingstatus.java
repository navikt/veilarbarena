package no.nav.fo.veilarbarena.soapproxy.oppfolgingstatus;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class Oppfolgingstatus {
    public final String oppfolgingsEnhet;
    public final String rettighetsgruppeKode;
    public final String formidlingsgruppeKode;
    public final String serviceGruppeKode;
    public final LocalDate inaktiveringsdato;
    public final Boolean harMottaOppgaveIArena;
}
