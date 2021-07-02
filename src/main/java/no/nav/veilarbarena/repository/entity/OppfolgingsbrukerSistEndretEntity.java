package no.nav.veilarbarena.repository.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class OppfolgingsbrukerSistEndretEntity {
    ZonedDateTime oppfolgingsbrukerSistEndring;
    String fodselsnr;
}
