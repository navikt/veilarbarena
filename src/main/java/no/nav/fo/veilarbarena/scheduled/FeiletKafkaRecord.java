package no.nav.fo.veilarbarena.scheduled;

import lombok.Value;
import no.nav.sbl.sql.mapping.QueryMapping;
import no.nav.sbl.sql.mapping.SqlRecord;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Value
public class FeiletKafkaRecord implements SqlRecord {
    QueryMapping.Column<String, String> fodselsnr;
    QueryMapping.Column<Timestamp, ZonedDateTime> tidspunkt_feilet;
}
