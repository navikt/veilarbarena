package no.nav.fo.veilarbarena.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Value;

import java.time.LocalDate;

@Value
public class OppfolgingsstatusDTO {
    @JsonAlias("rettighetsgruppeKode")
    String rettighetsgruppe;
    @JsonAlias("formidlingsgruppeKode")
    String formidlingsgruppe;
    @JsonAlias("servicegruppeKode")
    String servicegruppe;
    @JsonAlias("navOppfoelgingsenhet")
    String oppfolgingsenhet;
    @JsonAlias("inaktiveringsdato")
    LocalDate inaktiveringsdato;
    @JsonAlias("kanEnkeltReaktiveres")
    Boolean kanEnkeltReaktiveres;
}
