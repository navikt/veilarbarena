package no.nav.veilarbarena.utils;

import no.nav.pto_schema.enums.arena.*;
import no.nav.pto_schema.kafka.json.topic.onprem.EndringPaaOppfoelgingsBrukerV2;
import no.nav.veilarbarena.repository.entity.OppfolgingsbrukerEntity;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;

public class DtoMapperTest {

    @Test
    public void tilEndringPaaOppfoelgingsBrukerV2__should_map_correctly() {
        ZonedDateTime iservFraDato = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime doedFraDato = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime sistEndret = ZonedDateTime.now();

        OppfolgingsbrukerEntity entity = new OppfolgingsbrukerEntity()
                .setFornavn("test")
                .setEtternavn("testersen")
                .setFodselsnr("12345678900")
                .setFormidlingsgruppekode("ARBS")
                .setIservFraDato(iservFraDato)
                .setNavKontor("1234")
                .setKvalifiseringsgruppekode("BATT")
                .setRettighetsgruppekode("DAGP")
                .setHovedmaalkode("SKAFFEA")
                .setSikkerhetstiltakTypeKode("FTUS")
                .setFrKode("6")
                .setHarOppfolgingssak(false)
                .setSperretAnsatt(null)
                .setErDoed(true)
                .setDoedFraDato(doedFraDato)
                .setTimestamp(sistEndret);

        EndringPaaOppfoelgingsBrukerV2 expectedBrukerV2 = EndringPaaOppfoelgingsBrukerV2.builder()
                .fornavn("test")
                .etternavn("testersen")
                .fodselsnummer("12345678900")
                .formidlingsgruppe(Formidlingsgruppe.ARBS)
                .iservFraDato(iservFraDato.toLocalDate())
                .oppfolgingsenhet("1234")
                .kvalifiseringsgruppe(Kvalifiseringsgruppe.BATT)
                .rettighetsgruppe(Rettighetsgruppe.DAGP)
                .hovedmaal(Hovedmaal.SKAFFEA)
                .sikkerhetstiltakType(SikkerhetstiltakType.FTUS)
                .diskresjonskode("6")
                .harOppfolgingssak(false)
                .sperretAnsatt(null)
                .erDoed(true)
                .doedFraDato(doedFraDato.toLocalDate())
                .sistEndretDato(sistEndret)
                .build();

        assertEquals(expectedBrukerV2, DtoMapper.tilEndringPaaOppfoelgingsBrukerV2(entity));
    }

    @Test
    public void tilEndringPaaOppfoelgingsBrukerV2__should_map_correctly_with_null_values() {
        OppfolgingsbrukerEntity entity = new OppfolgingsbrukerEntity()
                .setFodselsnr("12345678900");

        EndringPaaOppfoelgingsBrukerV2 expectedBrukerV2 = EndringPaaOppfoelgingsBrukerV2.builder()
                .fodselsnummer("12345678900")
                .build();

        assertEquals(expectedBrukerV2, DtoMapper.tilEndringPaaOppfoelgingsBrukerV2(entity));
    }

}
