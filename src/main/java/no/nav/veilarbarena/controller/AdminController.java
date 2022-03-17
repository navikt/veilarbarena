package no.nav.veilarbarena.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.UserRole;
import no.nav.common.job.JobRunner;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    public final static String PTO_ADMIN_SERVICE_USER = "srvpto-admin";

    private final AuthContextHolder authContextHolder;

    private final OppdaterteBrukereRepository oppdaterteBrukereRepository;

    @PostMapping("/republiser/endring-pa-bruker/all")
    public String republiserTilstand() {
        sjekkTilgangTilAdmin();
        log.info("Starter republisering på alle brukere");

        //Bruker dato et år frem i tid for at løpende oppdateringer fra Arena skal få prioritet
        Date endringsDato = Date.valueOf(LocalDate.now().plusYears(1));
        return JobRunner.runAsync("legg-alle-brukere-pa-v2-topic",
                () -> oppdaterteBrukereRepository.insertAlleBrukereFraOppfolgingsbrukerTabellen(endringsDato)
        );
    }

    @PostMapping("/republiser/endring-pa-bruker/fra-dato")
    public String republiserTilstandFraDato(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fraDato
    ) {
        sjekkTilgangTilAdmin();
        log.info("Starter republisering på alle brukere fra dato {}", fraDato.toString());

        //Bruker dato et år frem i tid for at løpende oppdateringer fra Arena skal få prioritet
        Date endringsDato = Date.valueOf(LocalDate.now().plusYears(1));
        return JobRunner.runAsync("legg-brukere-fra-dato-pa-v2-topic",
                () -> oppdaterteBrukereRepository.insertBrukereFraOppfolgingsbrukerFraDato(endringsDato, fraDato)
        );
    }

    @PostMapping("/republiser/endring-pa-bruker")
    public String republiserTilstand(@RequestParam String fnr) {
        sjekkTilgangTilAdmin();
        log.info("Starter republisering på bruker");

        return JobRunner.runAsync("legg-bruker-pa-v2-topic",
                () -> oppdaterteBrukereRepository.insertOppdatering(fnr, Date.valueOf(LocalDate.now()))
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
