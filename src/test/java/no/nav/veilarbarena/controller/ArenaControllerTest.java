package no.nav.veilarbarena.controller;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArenaController.class)
class ArenaControllerTest {

    private final Fnr FNR = Fnr.of("123456");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ArenaService arenaService;

    @Test
    void hentAktiviteter__should_return_403_for_non_system_user() throws Exception {
        when(authService.erSystembruker()).thenReturn(false);

        mockMvc.perform(get("/api/arena/aktiviteter").queryParam("fnr", FNR.get()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(arenaService);
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
