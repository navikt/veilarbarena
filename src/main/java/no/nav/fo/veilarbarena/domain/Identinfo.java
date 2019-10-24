package no.nav.fo.veilarbarena.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Identinfo {
    public String ident;
    public String identgruppe;
    public Boolean gjeldende;
}
