package no.nav.veilarbarena.utils;

import lombok.SneakyThrows;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

public class DateUtils {

    public static ZonedDateTime convertTimestampToZonedDateTimeIfPresent(Timestamp date){
        return Optional.ofNullable(date).isPresent() ?
                date.toLocalDateTime().atZone(ZoneId.systemDefault()) : null ;
    }

    @SneakyThrows
    public static XMLGregorianCalendar convertToCalendar(LocalDate date) {
        if (date == null) {
            return null;
        }

        final GregorianCalendar gregorianCalendar = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    }

    public static LocalDate convertToLocalDate(XMLGregorianCalendar calendar) {
        if (calendar == null) {
            return null;
        }

        return LocalDate.of(calendar.getYear(), calendar.getMonth(), calendar.getDay());
    }

}
