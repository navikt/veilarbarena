package no.nav.fo.veilarbarena;

import io.vavr.control.Option;
import no.nav.dialogarena.config.fasit.DbCredentials;
import no.nav.dialogarena.config.fasit.FasitUtils;
import no.nav.dialogarena.config.fasit.TestEnvironment;

import static no.nav.fo.veilarbarena.config.DbConfig.VEILARBARENADB_PASSWORD;
import static no.nav.fo.veilarbarena.config.DbConfig.VEILARBARENADB_URL;
import static no.nav.fo.veilarbarena.config.DbConfig.VEILARBARENADB_USERNAME;

public class DatabaseTestContext {
    public static void setupContext(String miljo) {
        Option<DbCredentials> dbCredential = Option.of(miljo)
                .map(TestEnvironment::valueOf)
                .map(testEnvironment -> FasitUtils.getDbCredentials(testEnvironment, "veilarbportefolje"));

        if (dbCredential.isDefined()) {
            dbCredential.forEach(DatabaseTestContext::setDataSourceProperties);
        }
    }

    private static void setDataSourceProperties(DbCredentials dbCredentials) {
        System.setProperty(VEILARBARENADB_URL, dbCredentials.url);
        System.setProperty(VEILARBARENADB_USERNAME, dbCredentials.getUsername());
        System.setProperty(VEILARBARENADB_PASSWORD, dbCredentials.getPassword());
    }
}
