package no.nav.veilarbarena.controller.v2;

import no.nav.common.auth.context.AuthContextHolder;
import no.nav.common.auth.context.UserRole;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminV2Controller.class)
public class AdminV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthContextHolder authContextHolder;

    @MockBean
    private OppdaterteBrukereRepository oppdaterteBrukereRepository;

    private final Fnr FNR = Fnr.of("123");
    private final Fnr FNRTOM = Fnr.of("");
    @Test
    public void republiserEndringPaBruker__should_return_401_if_user_missing() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.empty());
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.SYSTEM));

        mockMvc.perform(post("/api/v2/admin/republiser/endring-pa-bruker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnr\":\""+FNRTOM.get()+"\"}"))
                .andExpect(status().is(401));
    }

    @Test
    public void republiserEndringPaBruker__should_return_401_if_role_missing() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvpto-admin"));
        when(authContextHolder.getRole()).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/admin/republiser/endring-pa-bruker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnr\":\""+FNRTOM.get()+"\"}"))
                .andExpect(status().is(401));
    }

    @Test
    public void republiserEndringPaBruker__should_return_403_if_not_pto_admin() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvmyapp"));
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.SYSTEM));

        mockMvc.perform(post("/api/v2/admin/republiser/endring-pa-bruker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(403));
    }

    @Test
    public void republiserEndringPaBruker__should_return_403_if_not_system_user() throws Exception {
        when(authContextHolder.getSubject()).thenReturn(Optional.of("srvpto-admin"));
        when(authContextHolder.getRole()).thenReturn(Optional.of(UserRole.EKSTERN));

        mockMvc.perform(post("/api/v2/admin/republiser/endring-pa-bruker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnr\":\""+FNR.get()+"\"}"))
                .andExpect(status().is(403));
    }
}
