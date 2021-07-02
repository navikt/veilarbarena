package no.nav.veilarbarena.client.ytelseskontrakt;

import no.nav.common.health.HealthCheck;
import no.nav.common.types.identer.Fnr;

import javax.xml.datatype.XMLGregorianCalendar;

public interface YtelseskontraktClient extends HealthCheck {

    YtelseskontraktResponse hentYtelseskontraktListe(Fnr personId, XMLGregorianCalendar periodeFom, XMLGregorianCalendar periodeTom);

    YtelseskontraktResponse hentYtelseskontraktListe(Fnr personId);

}
