package no.nav.fo.veilarbarena;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import no.nav.fo.veilarbarena.config.DbConfig;
import no.nav.fo.veilarbarena.utils.MigrationUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static no.nav.sbl.util.EnvironmentUtils.Type.PUBLIC;
import static no.nav.sbl.util.EnvironmentUtils.setProperty;

@Slf4j
public abstract class DbTest {

    private static AnnotationConfigApplicationContext annotationConfigApplicationContext;
    private static PlatformTransactionManager platformTransactionManager;
    private TransactionStatus transactionStatus;

    private static int counter;

    private static String imMemoryUrl() {
        return "jdbc:h2:mem:veilarbarena-" + (counter++) + ";DB_CLOSE_DELAY=-1;MODE=Oracle";
    }

    @SneakyThrows
    protected static void initSpringContext(Class... classes) {
        List<Class> list = new ArrayList<>(asList(DbConfig.class));
        list.addAll(asList(classes));

        setProperty(DbConfig.VEILARBARENADB_URL, imMemoryUrl(), PUBLIC);
        setProperty(DbConfig.VEILARBARENADB_USERNAME, "sa", PUBLIC);
        setProperty(DbConfig.VEILARBARENADB_PASSWORD, "password", PUBLIC);

        annotationConfigApplicationContext = new AnnotationConfigApplicationContext(list.toArray(new Class[]{}));
        annotationConfigApplicationContext.start();
        platformTransactionManager = annotationConfigApplicationContext.getBean(PlatformTransactionManager.class);

        final String sql = IOUtils.toString(DbTest.class.getResourceAsStream("/db-test.sql"), "UTF-8");
        log.info(sql);
        annotationConfigApplicationContext.getBean(JdbcTemplate.class).execute(sql);

        MigrationUtils.createTables(annotationConfigApplicationContext.getBean(DataSource.class));
    }

    @AfterAll
    @AfterClass
    public static void stopSpringContext() {
        ofNullable(annotationConfigApplicationContext).ifPresent(AbstractApplicationContext::stop);
    }

    @BeforeEach
    @Before
    public void injectAvhengigheter() {
        annotationConfigApplicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @BeforeEach
    @Before
    public void startTransaksjon() {
        transactionStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
    }

    @AfterEach
    @After
    public void rollbackTransaksjon() {
        if (platformTransactionManager != null) {
            platformTransactionManager.rollback(transactionStatus);
        }
    }
}
