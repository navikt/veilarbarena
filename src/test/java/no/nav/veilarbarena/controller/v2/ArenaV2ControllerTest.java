package no.nav.veilarbarena.controller.v2;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import no.nav.common.types.identer.EnhetId;
import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingssakDTO;
import no.nav.veilarbarena.client.ords.dto.RegistrerIkkeArbeidssokerDto;
import no.nav.veilarbarena.config.EnvironmentProperties;
import no.nav.veilarbarena.controller.response.ArenaStatusDTO;
import no.nav.veilarbarena.service.ArenaService;
import no.nav.veilarbarena.service.AuthService;
import no.nav.veilarbarena.service.PubliserOppfolgingsbrukerService;
import no.nav.veilarbarena.utils.TestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ArenaV2Controller.class)
class ArenaV2ControllerTest {

    private final Fnr FNR = Fnr.of("123456");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvironmentProperties environmentProperties;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ArenaService arenaService;

    @MockitoBean
    private PubliserOppfolgingsbrukerService publiserOppfolgingsbrukerService;


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
        verify(authService, times(1)).sjekkAtSystembrukerErWhitelistet("amt-tiltak", null, null, null, null, null, null, null,null);
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
    void hentAktiviteter__should_return_204_no_content_when_empty() throws Exception {
        when(authService.erSystembruker()).thenReturn(true);
        when(arenaService.hentArenaAktiviteter(any(Fnr.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v2/arena/hent-aktiviteter")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fnr\":\""+FNR.get()+"\"}")
        ).andExpect(status().is(204));
    }

   @Test
    void registrer_ikke_arbeidssoker_should_create_string_response() throws Exception {
       String resultMessage = "Ny bruker ble registrert ok som IARBS";
       String result = "{\"resultat\":\""+resultMessage+"\",\"kode\":\"OK_REGISTRERT_I_ARENA\"}";
       RegistrerIkkeArbeidssokerDto response = RegistrerIkkeArbeidssokerDto.okResult(resultMessage);

       when(arenaService.registrerIkkeArbeidssoker(FNR)).thenReturn(Optional.of(response));

       mockMvc.perform(post("/api/v2/arena/registrer-i-arena")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"fnr\":\"" + FNR.get() + "\"}")
       ).andExpect(status().is(200)).andExpect(content().string(result));
   }

    @Test
    void registrer_ikke_arbeidssoker_should_return_400_if_invalid_request() throws Exception {
        when(arenaService.registrerIkkeArbeidssoker(FNR)).thenThrow(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Fødselsnummer 22*******38 finnes ikke i Folkeregisteret"));

        mockMvc.perform(post("/api/v2/arena/registrer-i-arena")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"something\":\"something\"}")
        ).andExpect(status().is(400));
    }

   @Test
    void registrer_ikke_arbeidssoker_should_return_422_on_functional_errors() {
       Map<String, String> funksjonelleFeil = Map.of(
               "KAN_REAKTIVERES_FORENKLET", "Eksisterende bruker er ikke oppdatert da bruker kan reaktiveres forenklet som arbeidssøker",
               "FNR_FINNES_IKKE", "Fødselsnummer 22*******38 finnes ikke i Folkeregisteret"
       );
       funksjonelleFeil.forEach((kode, melding) -> {
           when(arenaService.registrerIkkeArbeidssoker(FNR)).thenReturn(Optional.of(RegistrerIkkeArbeidssokerDto.errorResult(melding)));

           try {
               mockMvc.perform(post("/api/v2/arena/registrer-i-arena")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"fnr\":\"" + FNR.get() + "\"}")
               ).andExpect(status().is(422)).andExpect(content().string(CoreMatchers.containsString("\"kode\":\""+kode+"\"")));
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
       });
    }

    @Test
    void BRUKER_ALLEREDE_IARBS_og_BRUKER_ALLEREDE_ARBS_skal_returnere_200_om_arena_returnerer_422() {
        Map<String, String> funksjonelleIkkeFeil = Map.of(
                "BRUKER_ALLEREDE_IARBS", "Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe IARBS",
                "BRUKER_ALLEREDE_ARBS", "Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe ARBS"
        );
        funksjonelleIkkeFeil.forEach((kode, melding) -> {
            when(arenaService.registrerIkkeArbeidssoker(FNR)).thenReturn(Optional.of(RegistrerIkkeArbeidssokerDto.errorResult(melding)));
            try {
                mockMvc.perform(post("/api/v2/arena/registrer-i-arena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnr\":\"" + FNR.get() + "\"}")
                ).andExpect(status().is(200)).andExpect(content().string(CoreMatchers.containsString("\"kode\":\""+kode+"\"")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void  BRUKER_ALLEREDE_IARBS_og_BRUKER_ALLEREDE_ARBS_skal_returnere_200_om_arena_returnerer_200() {
        Map<String, String> funksjonelleIkkeHeltRett = Map.of(
                "BRUKER_ALLEREDE_IARBS", "Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe IARBS",
                "BRUKER_ALLEREDE_ARBS", "Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe ARBS"
        );
        funksjonelleIkkeHeltRett.forEach((kode, melding) -> {
            when(arenaService.registrerIkkeArbeidssoker(FNR)).thenReturn(Optional.of(RegistrerIkkeArbeidssokerDto.okResult(melding)));
            try {
                mockMvc.perform(post("/api/v2/arena/registrer-i-arena")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fnr\":\"" + FNR.get() + "\"}")
                ).andExpect(status().is(200)).andExpect(content().string(CoreMatchers.containsString("\"kode\":\""+kode+"\"")));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
