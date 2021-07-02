package no.nav.veilarbarena.controller.response;

import lombok.Data;
import lombok.experimental.Accessors;
import no.nav.common.types.identer.EnhetId;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class ArenaStatusDTO {
    String formidlingsgruppe;
    String kvalifiseringsgruppe;
    String rettighetsgruppe;
    LocalDate iservFraDato;
    EnhetId oppfolgingsenhet;
}
