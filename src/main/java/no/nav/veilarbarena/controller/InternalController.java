package no.nav.veilarbarena.controller;

import no.nav.common.health.HealthCheck;
import no.nav.common.health.HealthCheckUtils;
import no.nav.common.health.selftest.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static no.nav.common.health.selftest.SelfTestUtils.checkAllParallel;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private final SelfTestChecks selftestChecks;

    @Autowired
    public InternalController(SelfTestChecks selftestChecks) {
        this.selftestChecks = selftestChecks;
    }

    @GetMapping("/isReady")
    public void isReady() {
        List<HealthCheck> healthChecks =
                selftestChecks.getSelfTestChecks().stream()
                        .filter(SelfTestCheck::isCritical)
                        .map(SelfTestCheck::getCheck)
                        .collect(toList());

        HealthCheckUtils.findFirstFailingCheck(healthChecks)
                .ifPresent((failedCheck) -> {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @GetMapping("/isAlive")
    public void isAlive() {}

    @GetMapping("/selftest")
    public ResponseEntity selftest() {
        List<SelftTestCheckResult> checkResults = checkAllParallel(selftestChecks.getSelfTestChecks());
        String html = SelftestHtmlGenerator.generate(checkResults);
        int status = SelfTestUtils.findHttpStatusCode(checkResults, true);

        return ResponseEntity
                .status(status)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

}
