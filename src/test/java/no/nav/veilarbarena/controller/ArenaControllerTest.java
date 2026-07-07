package no.nav.veilarbarena.controller;

import java.time.LocalDate;
import java.util.Optional;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.config.EnvironmentProperties;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArenaController.class)
class ArenaControllerTest {

    private final Fnr FNR = Fnr.of("123456");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvironmentProperties environmentProperties;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ArenaService arenaService;

    @Test
    void hentStatus__should_check_authorizaton_if_not_system_user() throws Exception {
        when(authService.erSystembruker()).thenReturn(false);
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", FNR.get()));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentStatus__should_check_whitelist_if_system_user() throws Exception {
        when(environmentProperties.getAmtTiltakClientId()).thenReturn("amt-tiltak");
        when(authService.erSystembruker()).thenReturn(true);
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", FNR.get()));

        verify(authService, times(1)).sjekkAtSystembrukerErWhitelistet("amt-tiltak", null, null, null, null, null, null);
    }

    @Test
    void hentStatus__should_check_authorization() throws Exception {
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", FNR.get()));

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

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", FNR.get()))
            .andExpect(status().is(200))
            .andExpect(content().json(json, true));
    }

    @Test
    void hentStatus__should_return_404_if_no_user_found() throws Exception {
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", FNR.get()))
                .andExpect(status().is(404));
    }



    @Test
    void hentKanEnkeltReaktiveres__should_check_authorization() throws Exception {
        when(arenaService.hentArenaStatus(FNR, false)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/kan-enkelt-reaktiveres").queryParam("fnr", FNR.get()));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentKanEnkeltReaktiveres__return_correct_json_and_status() throws Exception {
        String json = TestUtils.readTestResourceFile("controller/arena/kan-enkelt-reaktiveres-response.json");

        when(arenaService.hentKanEnkeltReaktiveres(FNR)).thenReturn(true);

        mockMvc.perform(get("/api/arena/kan-enkelt-reaktiveres").queryParam("fnr", FNR.get()))
                .andExpect(status().is(200))
                .andExpect(content().json(json, true));
    }


    @Test
    void hentOppfolgingssak__should_check_authorization() throws Exception {
        when(arenaService.hentArenaOppfolginssak(FNR)).thenReturn(Optional.of(new ArenaOppfolgingssakDTO("test")));

        mockMvc.perform(get("/api/arena/oppfolgingssak").queryParam("fnr", FNR.get()));

        verify(authService, times(1)).sjekkTilgang(FNR);
    }

    @Test
    void hentOppfolgingssak__should_return_correct_json_and_status() throws Exception {
        String json = TestUtils.readTestResourceFile("controller/arena/oppfolgingssak-response.json");

        when(arenaService.hentArenaOppfolginssak(FNR)).thenReturn(Optional.of(new ArenaOppfolgingssakDTO("test")));

        mockMvc.perform(get("/api/arena/oppfolgingssak").queryParam("fnr", FNR.get()))
                .andExpect(status().is(200))
                .andExpect(content().json(json, true));
    }

    @Test
    void hentOppfolgingssak__should_return_404_if_user_not_found() throws Exception {
        when(arenaService.hentArenaOppfolginssak(FNR)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/oppfolgingssak").queryParam("fnr", FNR.get()))
                .andExpect(status().is(404));
    }

    @Test
    void hentAktiviteter__should_return_204_no_content_when_empty() throws Exception {
        when(authService.erSystembruker()).thenReturn(true);
        when(arenaService.hentArenaAktiviteter(any(Fnr.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/aktiviteter")
                .queryParam("fnr", FNR.get())
        ).andExpect(status().is(204));
    }
}
