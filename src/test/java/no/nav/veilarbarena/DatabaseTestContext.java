package no.nav.veilarbarena;

import io.vavr.control.Option;
import no.nav.fasit.DbCredentials;
import no.nav.fasit.FasitUtils;
import no.nav.fasit.TestEnvironment;
import no.nav.veilarbarena.config.ApplicationConfig;
import no.nav.veilarbarena.config.DatabaseConfig;

public class DatabaseTestContext {
    public static void setupContext(String miljo) {
        Option<DbCredentials> dbCredential = Option.of(miljo)
                .map(TestEnvironment::valueOf)
                .map(testEnvironment -> FasitUtils.getDbCredentials(testEnvironment, ApplicationConfig.APPLICATION_NAME));

        if (dbCredential.isDefined()) {
            dbCredential.forEach(DatabaseTestContext::setDataSourceProperties);
        }
    }

    private static void setDataSourceProperties(DbCredentials dbCredentials) {
        System.setProperty(DatabaseConfig.VEILARBARENADB_URL, dbCredentials.url);
        System.setProperty(DatabaseConfig.VEILARBARENADB_USERNAME, dbCredentials.getUsername());
        System.setProperty(DatabaseConfig.VEILARBARENADB_PASSWORD, dbCredentials.getPassword());
    }
}
