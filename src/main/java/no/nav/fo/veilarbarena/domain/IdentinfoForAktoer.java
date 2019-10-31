package no.nav.fo.veilarbarena.domain;

import lombok.Value;

import java.util.List;

@Value
public class IdentinfoForAktoer {
    public List<Identinfo> identer;
    public String feilmelding;
}
