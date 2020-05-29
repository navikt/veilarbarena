package no.nav.veilarbarena.domain.api;

import com.fasterxml.jackson.annotation.JsonAlias;

public class OppfolgingssakDTO {
    @JsonAlias("saksId")
    String oppfolgingssakId;
}
