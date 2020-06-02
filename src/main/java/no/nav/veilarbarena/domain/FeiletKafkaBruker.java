package no.nav.veilarbarena.domain;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class FeiletKafkaBruker {
    String fodselsnr;
    LocalDateTime tidspunktFeilet;
}
