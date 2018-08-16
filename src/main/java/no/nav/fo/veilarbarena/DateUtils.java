package no.nav.fo.veilarbarena;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

public class DateUtils {
    public static LocalDate xmlGregorianCalendarToLocalDate(XMLGregorianCalendar inaktiveringsdato) {
        return Optional.ofNullable(inaktiveringsdato)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(GregorianCalendar::toZonedDateTime)
                .map(ZonedDateTime::toLocalDate).orElse(null);
    }

    public static ZonedDateTime timestampToZonedDateTime(Timestamp timestamp) {
        return timestamp.toLocalDateTime().atZone(ZoneId.systemDefault());
    }
}
