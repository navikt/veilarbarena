package no.nav.fo.veilarbarena.domain;

import lombok.Value;

@Value
public class Identinfo {
    public String ident;
    public String identgruppe;
    public Boolean gjeldende;
}
