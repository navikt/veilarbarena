package no.nav.veilarbarena.client.ords.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class ArenaOppfolgingsstatusDTO {
    String rettighetsgruppeKode;
    String formidlingsgruppeKode;
    String servicegruppeKode;
    String navOppfoelgingsenhet;
    LocalDate inaktiveringsdato;
    Boolean kanEnkeltReaktiveres;
}
