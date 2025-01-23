package no.nav.veilarbarena.controller.v2;

import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
import no.nav.veilarbarena.config.EnvironmentProperties;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static no.nav.veilarbarena.utils.DateUtils.convertToCalendar;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArenaV2Controller.class)
class ArenaV2ControllerTest {

    private final Fnr FNR = Fnr.of("123456");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvironmentProperties environmentProperties;

    @MockBean
    private AuthService authService;

    @MockBean
    private ArenaService arenaService;



    @Test
    void hentStatus__should_check_authorizaton_if_not_system_user() throws Exception {

        when(authService.erSystembruker()).thenReturn(false);
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentStatus__should_check_whitelist_if_system_user() throws Exception {
        when(environmentProperties.getAmtTiltakClientId()).thenReturn("amt-tiltak");
        when(authService.erSystembruker()).thenReturn(true);
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"));
        verify(authService, times(1)).sjekkAtSystembrukerErWhitelistet("amt-tiltak", null, null, null, null, null, null, null);
    }

    @Test
    void hentStatus__should_check_authorization() throws Exception {
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentStatus__should_return_correct_json_and_status() throws Exception {
        String json = TestUtils.readTestResourceFile("controller/arena/status-response.json");

        ArenaStatusDTO arenaStatusDTO = new ArenaStatusDTO()
                .setFormidlingsgruppe("ARBS")
                .setKvalifiseringsgruppe("VURDI")
                .setRettighetsgruppe("DAGP")
                .setIservFraDato(LocalDate.of(2021, 10, 15))
                .setOppfolgingsenhet(EnhetId.of("1234"));

        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.of(arenaStatusDTO));

        mockMvc.perform(post("/api/v2/arena/hent-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"))
            .andExpect(status().is(200))
            .andExpect(content().json(json, true));
    }


    @Test
    void hentStatus__should_support_forceSync() throws Exception {
        String json = TestUtils.readTestResourceFile("controller/arena/status-response.json");

        ArenaStatusDTO arenaStatusDTO = new ArenaStatusDTO()
                .setFormidlingsgruppe("ARBS")
                .setKvalifiseringsgruppe("VURDI")
                .setRettighetsgruppe("DAGP")
                .setIservFraDato(LocalDate.of(2021, 10, 15))
                .setOppfolgingsenhet(EnhetId.of("1234"));

        when(arenaService.hentArenaStatus(FNR, true)).thenReturn(Optional.of(arenaStatusDTO));

        mockMvc.perform(post("/api/v2/arena/hent-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("forceSync", true)
                        .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(200))
                .andExpect(content().json(json, true));
    }

    @Test
    void hentStatus__should_return_404_if_no_user_found() throws Exception {
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(404));
    }



    @Test
    void hentKanEnkeltReaktiveres__should_check_authorization() throws Exception {
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-kan-enkelt-reaktiveres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentKanEnkeltReaktiveres__return_correct_json_and_status() throws Exception {
        String json = TestUtils.readTestResourceFile("controller/arena/kan-enkelt-reaktiveres-response.json");

        when(arenaService.hentKanEnkeltReaktiveres(FNR)).thenReturn(true);

        mockMvc.perform(post("/api/v2/arena/hent-kan-enkelt-reaktiveres")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(200))
                .andExpect(content().json(json, true));
    }


    @Test
    void hentOppfolgingssak__should_check_authorization() throws Exception {
        when(arenaService.hentArenaOppfolginssak(FNR)).thenReturn(Optional.of(new ArenaOppfolgingssakDTO("test")));

        mockMvc.perform(post("/api/v2/arena/hent-oppfolgingssak")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentOppfolgingssak__should_return_correct_json_and_status() throws Exception {
        String json = TestUtils.readTestResourceFile("controller/arena/oppfolgingssak-response.json");

        when(arenaService.hentArenaOppfolginssak(FNR)).thenReturn(Optional.of(new ArenaOppfolgingssakDTO("test")));

        mockMvc.perform(post("/api/v2/arena/hent-oppfolgingssak")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(200))
                .andExpect(content().json(json, true));
    }

    @Test
    void hentOppfolgingssak__should_return_404_if_user_not_found() throws Exception {
        when(arenaService.hentArenaOppfolginssak(FNR)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-oppfolgingssak")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(404));
    }


    @Test
    void hentYtelser__should_check_authorization() throws Exception {
        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(post("/api/v2/arena/hent-ytelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(200));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentYtelser__return_400_if_fraDato_not_null_and_tilDato_null() throws Exception {
        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(post("/api/v2/arena/hent-ytelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}")
                .queryParam("fra", "2021-06-21")
        ).andExpect(status().is(400));
    }

    @Test
    void hentYtelser__return_400_if_fraDato_is_null_and_tilDato_not_null() throws Exception {
        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(post("/api/v2/arena/hent-ytelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}")
                .queryParam("til", "2021-06-21")
        ).andExpect(status().is(400));
    }

    @Test
    void hentYtelser__should_use_default_fraDato_and_tilDato() throws Exception {
        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(post("/api/v2/arena/hent-ytelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}")
        ).andExpect(status().is(200));

        LocalDate now = LocalDate.now();

        LocalDate expectedFraDato = now.minusMonths(2);
        LocalDate expectedTilDato = now.plusMonths(1);

        verify(arenaService, times(1)).hentYtelseskontrakt(FNR, expectedFraDato, expectedTilDato);
    }

    @Test
    void hentYtelser__should_pass_through_tilDato_and_fraDato() throws Exception {
        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(post("/api/v2/arena/hent-ytelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}")
                .queryParam("fra", "2021-06-28")
                .queryParam("til", "2021-08-09")
        ).andExpect(status().is(200));

        LocalDate expectedFraDato = LocalDate.of(2021, 6, 28);
        LocalDate expectedTilDato = LocalDate.of(2021, 8, 9);

        verify(arenaService, times(1)).hentYtelseskontrakt(FNR, expectedFraDato, expectedTilDato);
    }

    @Test
    void hentAktiviteter__should_return_204_no_content_when_empty() throws Exception {
        when(authService.erSystembruker()).thenReturn(true);
        when(arenaService.hentArenaAktiviteter(any(Fnr.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-aktiviteter")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}")
        ).andExpect(status().is(204));
    }

    @Test
    void hentYtelser__should_create_correct_response() throws Exception {
        String json = TestUtils.readTestResourceFile("controller/arena/ytelser-response.json");

        List<YtelseskontraktResponse.VedtakDto> vedtakDtoListe = new ArrayList<>();
        vedtakDtoListe.add(
                new YtelseskontraktResponse.VedtakDto()
                        .setVedtakstype("type")
                        .setStatus("status")
                        .setAktivitetsfase("aktivitetsfase")
                        .setRettighetsgruppe("rettighetsgruppe")
                        .setFraDato(convertToCalendar(LocalDate.of(2021, 4, 28)))
                        .setTilDato(convertToCalendar(LocalDate.of(2021, 8, 6)))
        );

        List<YtelseskontraktResponse.YtelseskontraktDto> ytelseListe = new ArrayList<>();
        ytelseListe.add(
                new YtelseskontraktResponse.YtelseskontraktDto()
                        .setYtelsestype("type")
                        .setStatus("status")
                        .setMotattDato(convertToCalendar(LocalDate.of(2021, 1, 13)))
                        .setFraDato(convertToCalendar(LocalDate.of(2021, 1, 15)))
                        .setTilDato(convertToCalendar(LocalDate.of(2021, 2, 3)))
        );

        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(vedtakDtoListe, ytelseListe));

        mockMvc.perform(post("/api/v2/arena/hent-ytelser")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}")
        ).andExpect(status().is(200)).andExpect(content().json(json, true));
    }

}
