package no.nav.veilarbarena;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Utils {

    public static User lagNyBruker() {
        ZonedDateTime TIDSPUNKT = new Timestamp(100000000000L).toLocalDateTime().atZone(ZoneId.systemDefault());
        return new User(
                PersonId.aktorId("test"),
                PersonId.fnr("test"),
                "test",
                "test",
                "test",
                "test",
                TIDSPUNKT,
                "test",
                "test",
                "test",
                "test",
                "test",
                false,
                false,
                false,
                TIDSPUNKT,
                TIDSPUNKT
        );
    }
}
