package no.nav.veilarbarena.controller;

import lombok.RequiredArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.YtelserDTO;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.service.YtelserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static no.nav.veilarbarena.utils.DtoMapper.mapTilYtelserDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ytelser")
public class YtelserController {

    private static final int MANEDER_BAK_I_TID = 2;

    private static final int MANEDER_FREM_I_TID = 1;

    private final AuthService authService;

    private final YtelserService ytelserService;

    @GetMapping
    public YtelserDTO hentYtelser(
            @RequestParam("fnr") Fnr fnr,
            @RequestParam(value = "fra", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fra,
            @RequestParam(value = "til", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate til
    ) {
        authService.sjekkTilgang(fnr);

        if (fra != null ^ til != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Både \"fra\" og \"til\" må settes eller ingen av de");
        } else if (fra == null) {
            // Hvis "fra" == null så vil alltid "til" være null
            fra = LocalDate.now().minusMonths(MANEDER_BAK_I_TID);
            til = LocalDate.now().plusMonths(MANEDER_FREM_I_TID);
        }

        var ytelseskontrakt = ytelserService.hentYtelseskontrakt(fnr, fra, til);

        return mapTilYtelserDTO(ytelseskontrakt);
    }

}
