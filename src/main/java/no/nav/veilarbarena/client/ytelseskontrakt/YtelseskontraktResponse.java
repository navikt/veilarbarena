package no.nav.veilarbarena.client.ytelseskontrakt;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YtelseskontraktResponse {
    List<Vedtak> vedtaksliste;
    List<Ytelseskontrakt> ytelser;

    @Data
    @Accessors(chain = true)
    public static class Vedtak {
        String vedtakstype;
        String status;
        String aktivitetsfase;
        String rettighetsgruppe;
        XMLGregorianCalendar fraDato;
        XMLGregorianCalendar tilDato;
    }

    @Data
    @Accessors(chain = true)
    public static class Ytelseskontrakt {
        String status;
        String ytelsestype;
        XMLGregorianCalendar motattDato;
        XMLGregorianCalendar fraDato;
        XMLGregorianCalendar tilDato;
    }

}
