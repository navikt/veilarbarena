package no.nav.veilarbarena.controller.v2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.veilarbarena.client.ords.dto.ArenaAktiviteterDTO;
import no.nav.veilarbarena.client.ords.dto.PersonRequest;
import no.nav.veilarbarena.client.ords.dto.RegistrerIkkeArbeidssokerDto;
import no.nav.veilarbarena.config.EnvironmentProperties;
import no.nav.veilarbarena.controller.response.*;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.service.PubliserOppfolgingsbrukerService;
import no.nav.veilarbarena.utils.DtoMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static no.nav.veilarbarena.utils.DtoMapper.mapTilYtelserDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/arena")
@Slf4j
public class ArenaV2Controller {

    private static final int MANEDER_BAK_I_TID = 2;
    private static final int MANEDER_FREM_I_TID = 1;
    private final AuthService authService;
    private final ArenaService arenaService;
    private final EnvironmentProperties environmentProperties;
    private final PubliserOppfolgingsbrukerService publiserOppfolgingsbrukerService;


    @PostMapping("/hent-status")
    public ArenaStatusDTO hentStatusV2(@RequestBody PersonRequest personRequest, @RequestHeader(value = "forceSync", required = false) boolean forceSync) {
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(personRequest.getFnr());
        } else {
            // TODO: Dette er en dårlig måte og sjekke tilganger på, bruk heller sjekk på access_as_application
            authService.sjekkAtSystembrukerErWhitelistet(
                    environmentProperties.getAmtTiltakClientId(),
                    environmentProperties.getAmtPersonServiceClientId(),
                    environmentProperties.getTiltaksgjennomforingApiClientId(),
                    environmentProperties.getVeilarbregistreringClientId(),
                    environmentProperties.getVeilarbregistreringClientIdGCP(),
                    environmentProperties.getPoaoTilgangGCPClientId(),
                    environmentProperties.getPoaoTilgangFSSClientId(),
                    environmentProperties.getAapOppgaveClientId(),
                    environmentProperties.getAapPostmottakClientId()
            );
        }

        return arenaService.hentArenaStatus(personRequest.getFnr(), forceSync)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/hent-kan-enkelt-reaktiveres")
    public KanEnkeltReaktiveresDTO hentKanEnkeltReaktiveresV2(@RequestBody PersonRequest personRequest) {
        if (!authService.erSystembruker()) {
            authService.sjekkTilgang(personRequest.getFnr());
        }

        Boolean kanEnkeltReaktivers = arenaService.hentKanEnkeltReaktiveres(personRequest.getFnr());

        return new KanEnkeltReaktiveresDTO(kanEnkeltReaktivers);
    }

    @PostMapping("/hent-oppfolgingssak")
    public OppfolgingssakDTO hentOppfolgingssakV2(@RequestBody PersonRequest personRequest) {
        authService.sjekkTilgang(personRequest.getFnr());

        return arenaService.hentArenaOppfolginssak(personRequest.getFnr())
                .map(DtoMapper::mapTilOppfolgingssakDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/hent-ytelser")
    public YtelserDTO hentYtelserV2(
            @RequestBody PersonRequest personRequest,
            @RequestParam(value = "fra", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fra,
            @RequestParam(value = "til", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate til
    ) {
        authService.sjekkTilgang(personRequest.getFnr());

        if (fra != null ^ til != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Både \"fra\" og \"til\" må settes eller ingen av de");
        } else if (fra == null) {
            // Hvis "fra" == null så vil alltid "til" være null
            fra = LocalDate.now().minusMonths(MANEDER_BAK_I_TID);
            til = LocalDate.now().plusMonths(MANEDER_FREM_I_TID);
        }

        var ytelseskontrakt = arenaService.hentYtelseskontrakt(personRequest.getFnr(), fra, til);

        return mapTilYtelserDTO(ytelseskontrakt);
    }

    @PostMapping("/hent-aktiviteter")
    public AktiviteterDTO hentAktiviteterV2(@RequestBody PersonRequest personRequest) {
        if (!authService.erSystembruker()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return arenaService.hentArenaAktiviteter(personRequest.getFnr())
                .map(this::mapArenaAktiviteter)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT));
    }

    /**
     * Registrer ikke-arbeidssøker i arena
     */
    @PostMapping("/registrer-i-arena")
    public ResponseEntity<RegistrerIkkeArbeidssokerDto> registrerIkkeArbeidssoker(@RequestBody PersonRequest personRequest) {

        authService.sjekkTilgang(personRequest.getFnr());
        RegistrerIkkeArbeidssokerDto registrert = arenaService.registrerIkkeArbeidssoker(personRequest.getFnr())
                .orElse(RegistrerIkkeArbeidssokerDto.errorResult("Bruker ikke registrert"));
        /*
        OK_REGISTRERT_I_ARENA,
        FNR_FINNES_IKKE,
        KAN_REAKTIVERES_FORENKLET,
        BRUKER_ALLEREDE_ARBS,
        BRUKER_ALLEREDE_IARBS,
        UKJENT_FEIL
         */
        return switch (registrert.getKode()) {
            case OK_REGISTRERT_I_ARENA -> {
                    try {
                        arenaService.refreshMaterializedOppfolgingsBrukerView();
                        log.info("Refeshet materialized view for oppfolgingsbruker");
                        var blePublisert = publiserOppfolgingsbrukerService.publiserOppfolgingsbruker(personRequest.getFnr().get());
                        if (!blePublisert) {
                            log.warn("Fant ingen oppfølgingsbruker å publisere på kafka");
                        }
                    } catch (Exception e) {
                        /* Skal fortsatt svare med OK, endringer vil propagert via OppfolgingsbrukerEndretSchedule istedet */
                        log.warn("Kunne ikke publisere nylig registrert oppfolgingsbruker på kafka", e);
                    }
                    yield new ResponseEntity<>(registrert, HttpStatus.OK);
            }
            case BRUKER_ALLEREDE_ARBS, BRUKER_ALLEREDE_IARBS ->
                    new ResponseEntity<>(registrert, HttpStatus.OK);
            case UKJENT_FEIL, FNR_FINNES_IKKE, KAN_REAKTIVERES_FORENKLET -> {
                log.warn("Kunne ikke registrere bruker i arena: {}", registrert.getResultat());
                yield new ResponseEntity<>(registrert, HttpStatus.UNPROCESSABLE_ENTITY);
            }
        };
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
