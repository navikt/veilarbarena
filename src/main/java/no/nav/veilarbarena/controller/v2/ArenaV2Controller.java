package no.nav.veilarbarena.controller.v2;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static no.nav.veilarbarena.utils.DtoMapper.mapTilYtelserDTO;
import static no.nav.veilarbarena.utils.FnrMaker.hentFnr;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/arena")
public class ArenaV2Controller {

    private static final int MANEDER_BAK_I_TID = 2;

    private static final int MANEDER_FREM_I_TID = 1;

    private final AuthService authService;

    private final ArenaService arenaService;

    private final EnvironmentProperties environmentProperties;


    @PostMapping("/status")
    public ArenaStatusDTO hentStatusV2(@RequestBody String fnr) {
        Fnr fodselsnummer = hentFnr(fnr);
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(fodselsnummer);
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

        return arenaService.hentArenaStatus(fodselsnummer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/kan-enkelt-reaktiveres")
    public KanEnkeltReaktiveresDTO hentKanEnkeltReaktiveresV2(@RequestBody String fnr) {
        Fnr fodselsnummer = hentFnr(fnr);
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(fodselsnummer);
        }

        Boolean kanEnkeltReaktivers = arenaService.hentKanEnkeltReaktiveres(fodselsnummer);

        return new KanEnkeltReaktiveresDTO(kanEnkeltReaktivers);
    }

    @PostMapping("/oppfolgingssak")
    public OppfolgingssakDTO hentOppfolgingssakV2(@RequestBody String fnr) {
        Fnr fodselsnummer = hentFnr(fnr);
        authService.sjekkTilgang(fodselsnummer);

        return arenaService.hentArenaOppfolginssak(fodselsnummer)
                .map(DtoMapper::mapTilOppfolgingssakDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/ytelser")
    public YtelserDTO hentYtelserV2(
            @RequestBody String fnr,
            @RequestParam(value = "fra", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fra,
            @RequestParam(value = "til", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate til
    ) {
        Fnr fodselsnummer = hentFnr(fnr);
        authService.sjekkTilgang(fodselsnummer);

        if (fra != null ^ til != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Både \"fra\" og \"til\" må settes eller ingen av de");
        } else if (fra == null) {
            // Hvis "fra" == null så vil alltid "til" være null
            fra = LocalDate.now().minusMonths(MANEDER_BAK_I_TID);
            til = LocalDate.now().plusMonths(MANEDER_FREM_I_TID);
        }

        var ytelseskontrakt = arenaService.hentYtelseskontrakt(fodselsnummer, fra, til);

        return mapTilYtelserDTO(ytelseskontrakt);
    }

    @PostMapping("/aktiviteter")
    public AktiviteterDTO hentAktiviteterV2(@RequestBody String fnr) {
        if (!authService.erSystembruker()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Fnr fodselsnummer = hentFnr(fnr);
        return arenaService.hentArenaAktiviteter(fodselsnummer)
                .map(this::mapArenaAktiviteter)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }
    private AktiviteterDTO mapArenaAktiviteter(ArenaAktiviteterDTO arenaAktiviteterDTO) {
        ArenaAktiviteterDTO.Response response = arenaAktiviteterDTO.getResponse();

        List<AktiviteterDTO.Tiltaksaktivitet> tiltaksaktiviteter = response.getTiltaksaktivitetListe()
                .stream()
                .map(this::mapTiltaksaktivitet)
                .collect(Collectors.toList());

        List<AktiviteterDTO.Gruppeaktivitet> gruppeaktiviteter = response.getGruppeaktivitetListe()
                .stream()
                .map(this::mapGruppeaktivitet)
                .collect(Collectors.toList());

        List<AktiviteterDTO.Utdanningsaktivitet> utdanningsaktiviteter = response.getUtdanningsaktivitetListe()
                .stream()
                .map(this::mapUtdanningsaktivitet)
                .collect(Collectors.toList());

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
            moteplanListe = a.getMoeteplanListe().stream().map(this::mapMoteplan).collect(Collectors.toList());
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
