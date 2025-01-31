package no.nav.veilarbarena.client.ords.dto;

import lombok.Data;
import lombok.NonNull;
import no.nav.common.types.identer.Fnr;

@Data
public class PersonRequest {
    @NonNull
    Fnr fnr;
}
