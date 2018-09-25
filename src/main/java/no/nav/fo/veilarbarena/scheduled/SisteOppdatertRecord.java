package no.nav.fo.veilarbarena.scheduled;

import lombok.Value;
import no.nav.sbl.sql.mapping.QueryMapping.Column;
import no.nav.sbl.sql.mapping.SqlRecord;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Value
public class SisteOppdatertRecord implements SqlRecord {
    Column<Timestamp, ZonedDateTime> oppfolgingsbruker_sist_endring;
    Column<String, String> fodselsnr;
}
