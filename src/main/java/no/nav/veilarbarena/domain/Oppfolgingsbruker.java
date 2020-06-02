package no.nav.veilarbarena.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Oppfolgingsbruker {

    public String fodselsnr;
    public String formidlingsgruppekode;
    public ZonedDateTime iservFraDato;
    public String navKontor;
    public String kvalifiseringsgruppekode;
    public String rettighetsgruppekode;
    public String hovedmaalkode;
    public String sikkerhetstiltakTypeKode;
    public String frKode;
    public Boolean harOppfolgingssak;
    public Boolean sperretAnsatt;
    public Boolean erDoed;
    public ZonedDateTime doedFraDato;

}

