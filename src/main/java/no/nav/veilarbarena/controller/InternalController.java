package no.nav.veilarbarena.controller;

import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthChecker;
import no.nav.common.health.selftest.SelfTestCheck;
import no.nav.common.health.selftest.SelfTestUtils;
import no.nav.common.health.selftest.SelftTestCheckResult;
import no.nav.common.health.selftest.SelftestHtmlGenerator;
import no.nav.veilarbarena.client.ArenaOrdsClient;
import no.nav.veilarbarena.utils.DatabaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static no.nav.common.health.selftest.SelfTestUtils.checkAllParallel;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final JdbcTemplate db;

    private final ArenaOrdsClient arenaOrdsClient;

    private final List<SelfTestCheck> selftestChecks;

    @Autowired
    public InternalController(JdbcTemplate db, ArenaOrdsClient arenaOrdsClient) {
        this.db = db;
        this.arenaOrdsClient = arenaOrdsClient;
        this.selftestChecks = Arrays.asList(
                new SelfTestCheck("Arena ORDS ping", true, arenaOrdsClient),
                new SelfTestCheck("Database ping", true, () -> DatabaseUtils.checkDbHealth(db))
        );
    }

    @GetMapping("/isReady")
    public void isReady() {
        List<HealthCheck> healthChecks = List.of(
                arenaOrdsClient,
                () -> DatabaseUtils.checkDbHealth(db)
        );

        HealthChecker.findFirstFailingCheck(healthChecks)
                .ifPresent((failedCheck) -> {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @GetMapping("/isAlive")
    public void isAlive() {}

    @GetMapping("/selftest")
    public ResponseEntity selftest() {
        List<SelftTestCheckResult> checkResults = checkAllParallel(selftestChecks);
        String html = SelftestHtmlGenerator.generate(checkResults);
        int status = SelfTestUtils.findHttpStatusCode(checkResults, true);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

}
