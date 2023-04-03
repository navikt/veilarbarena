package no.nav.veilarbarena.utils;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class LocalH2Database {

    private static JdbcTemplate db;

    public static JdbcTemplate getDb() {
        if (db == null) {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:veilarbarena-local;DB_CLOSE_DELAY=-1;MODE=Oracle;TRACE_LEVEL_SYSTEM_OUT=1;NON_KEYWORDS=KEY,VALUE,PARTITION;");

            db = new JdbcTemplate(dataSource);
            initDb(db);
        }


        return db;
    }

    private static void initDb(JdbcTemplate db) {
        String initSql = TestUtils.readTestResourceFile("db-init.sql");
        db.update(initSql);
    }

}
