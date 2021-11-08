package no.nav.veilarbarena.controller;

import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.UserRole;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static no.nav.veilarbarena.utils.TestUtils.verifiserAsynkront;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthContextHolder authContextHolder;

    @MockBean
    private OppdaterteBrukereRepository oppdaterteBrukereRepository;

    @Test
    public void republiserEndringPaBruker__should_return_401_if_user_missing() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.empty());
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.SYSTEM));

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker?fnr=123"))
                .andExpect(status().is(401));
    }

    @Test
    public void republiserEndringPaBruker__should_return_401_if_role_missing() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvpto-admin"));
        when(authContextHolder.getRole()).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker?fnr=123"))
                .andExpect(status().is(401));
    }

    @Test
    public void republiserEndringPaBruker__should_return_403_if_not_pto_admin() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvmyapp"));
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.SYSTEM));

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker?fnr=123"))
                .andExpect(status().is(403));
    }

    @Test
    public void republiserEndringPaBruker__should_return_403_if_not_system_user() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvpto-admin"));
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.EKSTERN));

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker?fnr=123"))
                .andExpect(status().is(403));
    }

    @Test
    public void republiserEndringPaBruker__should_return_job_id_and_republish() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvpto-admin"));
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.SYSTEM));

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker?fnr=123"))
                .andExpect(status().is(200))
                .andExpect(content().string(matchesPattern("^([a-f0-9]+)$")));

        verifiserAsynkront(3, TimeUnit.SECONDS,
                () -> verify(oppdaterteBrukereRepository, times(1)).insertOppdatering(eq("123"), any())
        );
    }

    @Test
    public void republiserEndringPaBruker__should_return_job_id_and_republish_all() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvpto-admin"));
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.SYSTEM));

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker/all"))
                .andExpect(status().is(200))
                .andExpect(content().string(matchesPattern("^([a-f0-9]+)$")));

        verifiserAsynkront(3, TimeUnit.SECONDS,
                () -> verify(oppdaterteBrukereRepository, times(1)).insertAlleBrukereFraOppfolgingsbrukerTabellen(any())
        );
    }

}
