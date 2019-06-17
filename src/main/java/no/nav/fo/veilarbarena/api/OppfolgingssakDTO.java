package no.nav.fo.veilarbarena.api;

import com.fasterxml.jackson.annotation.JsonAlias;

public class OppfolgingssakDTO {
    @JsonAlias("saksId")
    String oppfolgingssakId;
}
