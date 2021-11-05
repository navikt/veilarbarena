package no.nav.veilarbarena.repository.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.sql.Date;
import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class OppdatertBrukerEntity {
    Date tidsstempel;
    String fodselsnr;
}
