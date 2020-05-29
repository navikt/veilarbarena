package no.nav.veilarbarena.domain;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class FeiletKafkaRecord {
    String fodselsnr;
    LocalDateTime tidspunkt_feilet;
}
