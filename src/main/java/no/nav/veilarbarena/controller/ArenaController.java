package no.nav.veilarbarena.controller;

import lombok.RequiredArgsConstructor;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.config.EnvironmentProperties;
import no.nav.veilarbarena.controller.response.*;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static no.nav.veilarbarena.utils.DtoMapper.mapTilYtelserDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/arena")
public class ArenaController {

    private static final int MANEDER_BAK_I_TID = 2;

    private static final int MANEDER_FREM_I_TID = 1;

    private final AuthService authService;

    private final ArenaService arenaService;

    private final EnvironmentProperties environmentProperties;

    @GetMapping("/status")
    public ArenaStatusDTO hentStatus(@RequestParam("fnr") Fnr fnr) {
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(fnr);
        } else {
            // TODO: Dette er en dårlig måte og sjekke tilganger på, bruk heller sjekk på access_as_application
            authService.sjekkAtSystembrukerErWhitelistet(
                    environmentProperties.getAmtTiltakClientId(),
                    environmentProperties.getAmtPersonServiceClientId(),
                    environmentProperties.getTiltaksgjennomforingApiClientId(),
                    environmentProperties.getVeilarbregistreringClientId(),
                    environmentProperties.getVeilarbregistreringClientIdGCP(),
                    environmentProperties.getPoaoTilgangGCPClientId(),
                    environmentProperties.getPoaoTilgangFSSClientId()
            );
        }

        return arenaService.hentArenaStatus(fnr, false)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/kan-enkelt-reaktiveres")
    public KanEnkeltReaktiveresDTO hentKanEnkeltReaktiveres(@RequestParam("fnr") Fnr fnr) {
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(fnr);
        }

        Boolean kanEnkeltReaktivers = arenaService.hentKanEnkeltReaktiveres(fnr);

        return new KanEnkeltReaktiveresDTO(kanEnkeltReaktivers);
    }

    @GetMapping("/oppfolgingssak")
    public OppfolgingssakDTO hentOppfolgingssak(@RequestParam("fnr") Fnr fnr) {
        authService.sjekkTilgang(fnr);

        return arenaService.hentArenaOppfolginssak(fnr)
                .map(DtoMapper::mapTilOppfolgingssakDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/ytelser")
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

        var ytelseskontrakt = arenaService.hentYtelseskontrakt(fnr, fra, til);

        return mapTilYtelserDTO(ytelseskontrakt);
    }

    @GetMapping("/aktiviteter")
    public AktiviteterDTO hentAktiviteter(@RequestParam("fnr") Fnr fnr) {
        if (!authService.erSystembruker()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return arenaService.hentArenaAktiviteter(fnr)
                .map(this::mapArenaAktiviteter)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }

    private AktiviteterDTO mapArenaAktiviteter(ArenaAktiviteterDTO arenaAktiviteterDTO) {
        ArenaAktiviteterDTO.Response response = arenaAktiviteterDTO.getResponse();

        List<AktiviteterDTO.Tiltaksaktivitet> tiltaksaktiviteter = response.getTiltaksaktivitetListe()
                .stream()
                .map(this::mapTiltaksaktivitet)
                .toList();

        List<AktiviteterDTO.Gruppeaktivitet> gruppeaktiviteter = response.getGruppeaktivitetListe()
                .stream()
                .map(this::mapGruppeaktivitet)
                .toList();

        List<AktiviteterDTO.Utdanningsaktivitet> utdanningsaktiviteter = response.getUtdanningsaktivitetListe()
                .stream()
                .map(this::mapUtdanningsaktivitet)
                .toList();

        return new AktiviteterDTO()
                .setTiltaksaktiviteter(tiltaksaktiviteter)
                .setGruppeaktiviteter(gruppeaktiviteter)
                .setUtdanningsaktiviteter(utdanningsaktiviteter);
    }

    private AktiviteterDTO.Tiltaksaktivitet mapTiltaksaktivitet(ArenaAktiviteterDTO.Tiltaksaktivitet a) {
        return new AktiviteterDTO.Tiltaksaktivitet()
                .setTiltaksnavn(a.getTiltaksnavn())
                .setAktivitetId(a.getAktivitetId())
                .setTiltakLokaltNavn(a.getTiltakLokaltNavn())
                .setArrangor(a.getArrangoer())
                .setBedriftsnummer(a.getBedriftsnummer())
                .setDeltakelsePeriode(
                        Optional.ofNullable(a.getDeltakelsePeriode()).map(
                                dp ->  new AktiviteterDTO.Tiltaksaktivitet.DeltakelsesPeriode()
                                        .setFom(dp.getFom())
                                        .setTom(dp.getTom())
                        ).orElse(null)
                )
                .setDeltakelseProsent(a.getDeltakelseProsent() != null ? a.getDeltakelseProsent().intValue() : null)
                .setDeltakerStatus(a.getDeltakerStatus())
                .setStatusSistEndret(a.getStatusSistEndret())
                .setBegrunnelseInnsoking(a.getBegrunnelseInnsoeking())
                .setAntallDagerPerUke(a.getAntallDagerPerUke());
    }

    private AktiviteterDTO.Gruppeaktivitet mapGruppeaktivitet(ArenaAktiviteterDTO.Gruppeaktivitet a) {
        List<AktiviteterDTO.Gruppeaktivitet.Moteplan> moteplanListe = null;
        if (a.getMoeteplanListe() != null) {
            moteplanListe = a.getMoeteplanListe().stream().map(this::mapMoteplan).toList();
        }
        return new AktiviteterDTO.Gruppeaktivitet()
                .setAktivitetId(a.getAktivitetId())
                .setAktivitetstype(a.getAktivitetstype())
                .setBeskrivelse(a.getBeskrivelse())
                .setStatus(a.getStatus())
                .setMoteplanListe(moteplanListe);
    }

    private AktiviteterDTO.Gruppeaktivitet.Moteplan mapMoteplan(ArenaAktiviteterDTO.Gruppeaktivitet.Moteplan moteplan) {
        return new AktiviteterDTO.Gruppeaktivitet.Moteplan()
                .setStartDato(moteplan.getStartDato())
                .setStartKlokkeslett(moteplan.getStartKlokkeslett())
                .setSluttDato(moteplan.getSluttDato())
                .setSluttKlokkeslett(moteplan.getSluttKlokkeslett())
                .setSted(moteplan.getSted());
    }

    private AktiviteterDTO.Utdanningsaktivitet mapUtdanningsaktivitet(ArenaAktiviteterDTO.Utdanningsaktivitet a) {
        return new AktiviteterDTO.Utdanningsaktivitet()
                .setAktivitetId(a.getAktivitetId())
                .setAktivitetstype(a.getAktivitetstype())
                .setBeskrivelse(a.getBeskrivelse())
                .setAktivitetPeriode(
                        new AktiviteterDTO.Utdanningsaktivitet.AktivitetPeriode()
                                .setFom(a.getAktivitetPeriode().getFom())
                                .setTom(a.getAktivitetPeriode().getTom())
                );
    }

}
