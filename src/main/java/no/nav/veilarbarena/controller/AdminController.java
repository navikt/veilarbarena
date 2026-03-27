package no.nav.veilarbarena.controller;

import lombok.RequiredArgsConstructor;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.job.JobRunner;
import no.nav.common.utils.EnvironmentUtils;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private static final String POAO_ADMIN = String.format("%s-gcp:poao:poao-admin",
            EnvironmentUtils.isProduction().orElse(false) ? "prod" : "dev");

    private final AuthContextHolder authContextHolder;

    private final OppdaterteBrukereRepository oppdaterteBrukereRepository;

    @PostMapping("/republiser/endring-pa-bruker/all")
    public String republiserTilstand() {
        sjekkTilgangTilAdmin();
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
        //Bruker dato et år frem i tid for at løpende oppdateringer fra Arena skal få prioritet
        Date endringsDato = Date.valueOf(LocalDate.now().plusYears(1));
        return JobRunner.runAsync("legg-brukere-fra-dato-pa-v2-topic",
                () -> oppdaterteBrukereRepository.insertBrukereFraOppfolgingsbrukerFraDato(endringsDato, fraDato)
        );
    }

    record RepubliserRequest(List<String> fnrs) {}

    @PostMapping("/republiser/endring-pa-bruker")
    public String republiserTilstand(@RequestBody RepubliserRequest request) {
        sjekkTilgangTilAdmin();
        return JobRunner.runAsync("legg-bruker-pa-v2-topic",
                () -> request.fnrs().forEach(fnr ->
                        oppdaterteBrukereRepository.insertOppdatering(fnr, Date.valueOf(LocalDate.now()))
                )
        );
    }

    private void sjekkTilgangTilAdmin() {
        boolean erInternBruker = authContextHolder.erInternBruker();
        String azpName = authContextHolder.getIdTokenClaims()
                .map(claims -> {
                    try { return claims.getStringClaim("azp_name"); }
                    catch (Exception e) { return null; }
                })
                .orElse("");

        if (erInternBruker && POAO_ADMIN.equals(azpName)) return;

        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

}
