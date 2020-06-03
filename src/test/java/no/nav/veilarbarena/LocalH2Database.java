package no.nav.veilarbarena;

import no.nav.veilarbarena.utils.TestUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class LocalH2Database {

    private static JdbcDataSource dataSource;

    private static JdbcTemplate db;

    public static JdbcDataSource getDataSource() {
        if (dataSource == null) {
            dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:veilarbarena-local;DB_CLOSE_DELAY=-1;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=3");
        }

        return dataSource;
    }

    public static JdbcTemplate getDb() {
        if (db == null) {
            db = new JdbcTemplate(getDataSource());
            initDb(db);
        }

        return db;
    }

    private static void initDb(JdbcTemplate db) {
        String initSql = TestUtils.readTestResourceFile("db-init.sql");
        db.update(initSql);
    }

}
