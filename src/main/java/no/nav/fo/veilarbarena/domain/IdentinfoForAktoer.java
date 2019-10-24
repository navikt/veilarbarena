package no.nav.fo.veilarbarena.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentinfoForAktoer {
    public List<Identinfo> identer;
    public String feilmelding;
}
