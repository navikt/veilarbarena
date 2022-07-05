package no.nav.veilarbarena.service;

import no.nav.common.types.identer.Fnr;
import no.nav.veilarbarena.client.ords.ArenaOrdsClient;
import no.nav.veilarbarena.client.ords.dto.ArenaOppfolgingsstatusDTO;
import no.nav.veilarbarena.client.ytelseskontrakt.YtelseskontraktClient;
import no.nav.veilarbarena.repository.OppfolgingsbrukerRepository;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import no.nav.veilarbarena.utils.DtoMapper;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ArenaServiceTest {

    private final ArenaOrdsClient arenaOrdsClient = mock(ArenaOrdsClient.class);

    private final OppfolgingsbrukerRepository oppfolgingsbrukerRepository = mock(OppfolgingsbrukerRepository.class);

    private final YtelseskontraktClient ytelseskontraktClient = mock(YtelseskontraktClient.class);

    private final ArenaService arenaService = new ArenaService(arenaOrdsClient, oppfolgingsbrukerRepository, ytelseskontraktClient);

    @Test
    public void hentArenaStatus__skal_returnere_status_fra_database_hvis_funnet() {
        Fnr fnr = Fnr.of("1234554");

        var oppfolgingsbruker = new OppfolgingsbrukerEntity()
                .setFodselsnr(fnr.get())
                .setErDoed(false)
                .setFornavn("test")
                .setEtternavn("test");

        when(oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get())).thenReturn(Optional.of(oppfolgingsbruker));

        var maybeArenaStatus = arenaService.hentArenaStatus(fnr);

        assertTrue(maybeArenaStatus.isPresent());
        assertEquals(DtoMapper.mapTilArenaStatusDTO(oppfolgingsbruker), maybeArenaStatus.get());

        verifyNoInteractions(arenaOrdsClient);
    }

    @Test
    public void hentArenaStatus__skal_returnere_status_fra_arena_hvis_ikke_funnet_i_database() {
        Fnr fnr = Fnr.of("1234554");

        var oppfolgingsstatus = new ArenaOppfolgingsstatusDTO()
                .setFormidlingsgruppeKode("test")
                .setInaktiveringsdato(LocalDate.now())
                .setNavOppfoelgingsenhet("1234");

        when(oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get())).thenReturn(Optional.empty());

        when(arenaOrdsClient.hentArenaOppfolgingsstatus(fnr)).thenReturn(Optional.of(oppfolgingsstatus));

        var maybeArenaStatus = arenaService.hentArenaStatus(fnr);

        assertTrue(maybeArenaStatus.isPresent());
        assertEquals(DtoMapper.mapTilArenaStatusDTO(oppfolgingsstatus), maybeArenaStatus.get());
    }

    @Test
    public void hentArenaStatus__skal_returnere_empty_hvis_bruker_ikke_funnet() {
        Fnr fnr = Fnr.of("1234554");

        when(oppfolgingsbrukerRepository.hentOppfolgingsbruker(fnr.get())).thenReturn(Optional.empty());

        when(arenaOrdsClient.hentArenaOppfolgingsstatus(fnr)).thenReturn(Optional.empty());

        var maybeArenaStatus = arenaService.hentArenaStatus(fnr);

        assertTrue(maybeArenaStatus.isEmpty());
    }


}
