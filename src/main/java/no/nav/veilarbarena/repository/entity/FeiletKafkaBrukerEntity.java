package no.nav.veilarbarena.repository.entity;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class FeiletKafkaBrukerEntity {
    String fodselsnr;
    LocalDateTime tidspunktFeilet;
}
