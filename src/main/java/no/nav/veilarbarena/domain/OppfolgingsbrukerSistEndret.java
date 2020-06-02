package no.nav.veilarbarena.domain;

import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class OppfolgingsbrukerSistEndret {

    ZonedDateTime oppfolgingsbrukerSistEndring;
    String fodselsnr;

}
