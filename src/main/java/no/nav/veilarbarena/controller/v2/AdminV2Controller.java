package no.nav.veilarbarena.controller.v2;

import lombok.RequiredArgsConstructor;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.UserRole;
import no.nav.common.job.JobRunner;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminV2Controller {

    public final static String PTO_ADMIN_SERVICE_USER = "srvpto-admin";

    private final AuthContextHolder authContextHolder;

    private final OppdaterteBrukereRepository oppdaterteBrukereRepository;

    @PostMapping("/republiser/endring-pa-bruker/uten-fnr-i-url")
    public String republiserTilstandV2(@RequestBody String fnr) {
        sjekkTilgangTilAdmin();
        JSONObject request = new JSONObject(fnr);
        return JobRunner.runAsync("legg-bruker-pa-v2-topic",
                () -> oppdaterteBrukereRepository.insertOppdatering(request.getString("fnr"), Date.valueOf(LocalDate.now()))
        );
    }
    private void sjekkTilgangTilAdmin() {
        String subject = authContextHolder.getSubject()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        UserRole role = authContextHolder.getRole()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!PTO_ADMIN_SERVICE_USER.equals(subject) || !role.equals(UserRole.SYSTEM)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

}
