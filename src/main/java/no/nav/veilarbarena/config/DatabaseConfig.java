package no.nav.veilarbarena.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import no.nav.common.utils.Credentials;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static no.nav.common.utils.NaisUtils.getCredentials;
import static no.nav.common.utils.NaisUtils.getFileContent;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    private final Credentials oracleCredentials;

    public DatabaseConfig() {
        oracleCredentials = getCredentials("oracle_creds");
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        var jdbcUrl = getFileContent("/var/run/secrets/nais.io/oracle_config/jdbc_url");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(oracleCredentials.username);
        config.setPassword(oracleCredentials.password);
        config.setMaximumPoolSize(5);

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate db(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
