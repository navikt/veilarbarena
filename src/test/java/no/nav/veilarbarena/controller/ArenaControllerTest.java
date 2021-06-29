package no.nav.veilarbarena.controller;

import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArenaController.class)
public class ArenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private ArenaService arenaService;

    @Test
    public void hentStatus__should_check_authorization() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentArenaStatus(fnr)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", fnr.get()));

        verify(authService, times(1)).sjekkTilgang(fnr);
    }

    @Test
    public void hentStatus__should_return_correct_json_and_status() throws Exception {
        Fnr fnr = Fnr.of("123456");

        String json = TestUtils.readTestResourceFile("controller/arena/status-response.json");

        ArenaStatusDTO arenaStatusDTO = new ArenaStatusDTO()
                .setFormidlingsgruppe("ARBS")
                .setKvalifiseringsgruppe("VURDI")
                .setRettighetsgruppe("DAGP")
                .setIservFraDato(LocalDate.of(2021, 10, 15))
                .setOppfolgingsenhet(EnhetId.of("1234"));

        when(arenaService.hentArenaStatus(fnr)).thenReturn(Optional.of(arenaStatusDTO));

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", fnr.get()))
            .andExpect(status().is(200))
            .andExpect(content().json(json, true));
    }

    @Test
    public void hentStatus__should_return_404_if_no_user_found() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentArenaStatus(fnr)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/status").queryParam("fnr", fnr.get()))
                .andExpect(status().is(404));
    }



    @Test
    public void hentKanEnkeltReaktiveres__should_check_authorization() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentArenaStatus(fnr)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/arena/kan-enkelt-reaktiveres").queryParam("fnr", fnr.get()));

        verify(authService, times(1)).sjekkTilgang(fnr);
    }

    @Test
    public void hentKanEnkeltReaktiveres__return_correct_json_and_status() throws Exception {
        Fnr fnr = Fnr.of("123456");

        String json = TestUtils.readTestResourceFile("controller/arena/kan-enkelt-reaktiveres-response.json");

        when(arenaService.hentKanEnkeltReaktiveres(fnr)).thenReturn(true);

        mockMvc.perform(get("/api/arena/kan-enkelt-reaktiveres").queryParam("fnr", fnr.get()))
                .andExpect(status().is(200))
                .andExpect(content().json(json, true));
    }

}
