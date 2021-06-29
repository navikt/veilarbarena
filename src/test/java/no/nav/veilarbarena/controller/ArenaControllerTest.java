package no.nav.veilarbarena.controller;

import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static no.nav.veilarbarena.utils.DateUtils.convertToCalendar;
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



    @Test
    public void hentYtelser__should_check_authorization() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(get("/api/arena/ytelser").queryParam("fnr", fnr.get()))
                .andExpect(status().is(200));

        verify(authService, times(1)).sjekkTilgang(fnr);
    }

    @Test
    public void hentYtelser__return_400_if_fraDato_not_null_and_tilDato_null() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(get("/api/arena/ytelser")
                .queryParam("fnr", fnr.get())
                .queryParam("fra", "2021-06-21")
        ).andExpect(status().is(400));
    }

    @Test
    public void hentYtelser__return_400_if_fraDato_is_null_and_tilDato_not_null() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(get("/api/arena/ytelser")
                .queryParam("fnr", fnr.get())
                .queryParam("til", "2021-06-21")
        ).andExpect(status().is(400));
    }

    @Test
    public void hentYtelser__should_use_default_fraDato_and_tilDato() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(get("/api/arena/ytelser")
                .queryParam("fnr", fnr.get())
        ).andExpect(status().is(200));

        LocalDate now = LocalDate.now();

        LocalDate expectedFraDato = now.minusMonths(2);
        LocalDate expectedTilDato = now.plusMonths(1);

        verify(arenaService, times(1)).hentYtelseskontrakt(fnr, expectedFraDato, expectedTilDato);
    }

    @Test
    public void hentYtelser__should_pass_through_tilDato_and_fraDato() throws Exception {
        Fnr fnr = Fnr.of("123456");

        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(emptyList(), emptyList()));

        mockMvc.perform(get("/api/arena/ytelser")
                .queryParam("fnr", fnr.get())
                .queryParam("fra", "2021-06-28")
                .queryParam("til", "2021-08-09")
        ).andExpect(status().is(200));

        LocalDate expectedFraDato = LocalDate.of(2021, 6, 28);
        LocalDate expectedTilDato = LocalDate.of(2021, 8, 9);

        verify(arenaService, times(1)).hentYtelseskontrakt(fnr, expectedFraDato, expectedTilDato);
    }

    @Test
    public void hentYtelser__should_create_correct_response() throws Exception {
        Fnr fnr = Fnr.of("123456");

        String json = TestUtils.readTestResourceFile("controller/ytelser/ytelser-response.json");

        List<YtelseskontraktResponse.Vedtak> vedtakListe = new ArrayList<>();
        vedtakListe.add(
                new YtelseskontraktResponse.Vedtak()
                        .setVedtakstype("type")
                        .setStatus("status")
                        .setAktivitetsfase("aktivitetsfase")
                        .setRettighetsgruppe("rettighetsgruppe")
                        .setFraDato(convertToCalendar(LocalDate.of(2021, 4, 28)))
                        .setTilDato(convertToCalendar(LocalDate.of(2021, 8, 6)))
        );

        List<YtelseskontraktResponse.Ytelseskontrakt> ytelseListe = new ArrayList<>();
        ytelseListe.add(
                new YtelseskontraktResponse.Ytelseskontrakt()
                        .setYtelsestype("type")
                        .setStatus("status")
                        .setMotattDato(convertToCalendar(LocalDate.of(2021, 1, 13)))
                        .setFraDato(convertToCalendar(LocalDate.of(2021, 1, 15)))
                        .setTilDato(convertToCalendar(LocalDate.of(2021, 2, 3)))
        );

        when(arenaService.hentYtelseskontrakt(any(), any(), any())).thenReturn(new YtelseskontraktResponse(vedtakListe, ytelseListe));

        mockMvc.perform(get("/api/arena/ytelser")
                .queryParam("fnr", fnr.get())
        ).andExpect(status().is(200)).andExpect(content().json(json, true));
    }

}
