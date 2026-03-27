package no.nav.veilarbarena.controller;

import com.nimbusds.jwt.JWTClaimsSet;
import no.nav.common.auth.context.AuthContextHolder;
import no.nav.veilarbarena.repository.OppdaterteBrukereRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static no.nav.veilarbarena.utils.TestUtils.verifiserAsynkront;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthContextHolder authContextHolder;

    @MockBean
    private OppdaterteBrukereRepository oppdaterteBrukereRepository;

    private void mockPoaoAdminAuth() {
        when(authContextHolder.erInternBruker()).thenReturn(true);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("azp_name", "dev-gcp:poao:poao-admin")
                .build();
        when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));
    }

    @Test
    void republiserEndringPaBruker__should_return_403_if_not_intern_bruker() throws Exception {
        when(authContextHolder.erInternBruker()).thenReturn(false);
        when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnrs\":[\"123\"]}"))
                .andExpect(status().is(403));
    }

    @Test
    void republiserEndringPaBruker__should_return_403_if_not_poao_admin() throws Exception {
        when(authContextHolder.erInternBruker()).thenReturn(true);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("azp_name", "dev-gcp:poao:some-other-app")
                .build();
        when(authContextHolder.getIdTokenClaims()).thenReturn(Optional.of(claims));

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnrs\":[\"123\"]}"))
                .andExpect(status().is(403));
    }

    @Test
    void republiserEndringPaBruker__should_return_job_id_and_republish() throws Exception {
        mockPoaoAdminAuth();

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnrs\":[\"123\"]}"))
                .andExpect(status().is(200))
                .andExpect(content().string(matchesPattern("^([a-f0-9]+)$")));

        verifiserAsynkront(3, TimeUnit.SECONDS,
                () -> verify(oppdaterteBrukereRepository, times(1)).insertOppdatering(eq("123"), any())
        );
    }

    @Test
    void republiserEndringPaBruker__should_return_job_id_and_republish_all() throws Exception {
        mockPoaoAdminAuth();

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker/all"))
                .andExpect(status().is(200))
                .andExpect(content().string(matchesPattern("^([a-f0-9]+)$")));

        verifiserAsynkront(3, TimeUnit.SECONDS,
                () -> verify(oppdaterteBrukereRepository, times(1)).insertAlleBrukereFraOppfolgingsbrukerTabellen(any())
        );
    }

    @Test
    void republiserTilstandFraDato__returnerer_job_id_og_insert_brukere_for_republisering_med_fra_dato() throws Exception {
        mockPoaoAdminAuth();

        mockMvc.perform(post("/api/admin/republiser/endring-pa-bruker/fra-dato?fraDato=2021-10-17"))
                .andExpect(status().is(200))
                .andExpect(content().string(matchesPattern("^([a-f0-9]+)$")));

        verifiserAsynkront(3, TimeUnit.SECONDS,
                () -> verify(oppdaterteBrukereRepository, times(1))
                        .insertBrukereFraOppfolgingsbrukerFraDato(any(), eq(LocalDate.of(2021, 10, 17)))
        );
    }
}
