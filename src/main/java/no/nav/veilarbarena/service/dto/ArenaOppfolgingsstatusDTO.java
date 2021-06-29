package no.nav.veilarbarena.service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ArenaOppfolgingsstatusDTO {
    String rettighetsgruppeKode;
    String formidlingsgruppeKode;
    String servicegruppeKode;
    String navOppfoelgingsenhet;
    LocalDate inaktiveringsdato;
    Boolean kanEnkeltReaktiveres;
}
