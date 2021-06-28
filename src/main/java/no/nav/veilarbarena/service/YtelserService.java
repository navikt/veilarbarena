package no.nav.veilarbarena.service;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClient;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

import static no.nav.veilarbarena.utils.DateUtils.convertToCalendar;

@Service
public class YtelserService {

    private final YtelseskontraktClient ytelseskontraktClient;

    @Autowired
    public YtelserService(YtelseskontraktClient ytelseskontraktClient) {
        this.ytelseskontraktClient = ytelseskontraktClient;
    }

    public YtelseskontraktResponse hentYtelseskontrakt(Fnr fnr, LocalDate fra, LocalDate til) {
        XMLGregorianCalendar fomCalendar = convertToCalendar(fra);
        XMLGregorianCalendar tomCalendar = convertToCalendar(til);

        return ytelseskontraktClient.hentYtelseskontraktListe(fnr, fomCalendar, tomCalendar);
    }

}
