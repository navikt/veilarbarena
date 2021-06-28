package no.nav.veilarbarena.utils;

import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

public class DateUtilsTest {

    @Test
    public void convertToLocalDate__should_convert_date() throws DatatypeConfigurationException {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar(2021, Calendar.JUNE, 22);
        XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

        assertEquals(LocalDate.of(2021, 6, 22), DateUtils.convertToLocalDate(calendar));
    }

    @Test
    public void convertToCalendar__should_convert_date() throws DatatypeConfigurationException {
        final GregorianCalendar gregorianCalendar = new GregorianCalendar(2021, Calendar.JUNE, 22);
        XMLGregorianCalendar expectedCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);

        assertEquals(expectedCalendar, DateUtils.convertToCalendar(LocalDate.of(2021, 6, 22)));
    }

}
