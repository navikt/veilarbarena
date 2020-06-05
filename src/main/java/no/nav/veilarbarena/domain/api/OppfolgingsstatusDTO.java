package no.nav.veilarbarena.domain.api;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OppfolgingsstatusDTO {
    String rettighetsgruppe;
    String formidlingsgruppe;
    String servicegruppe;
    String oppfolgingsenhet;
    LocalDate inaktiveringsdato;
    Boolean kanEnkeltReaktiveres;
}
