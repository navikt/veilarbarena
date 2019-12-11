package no.nav.fo.veilarbarena.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OppfolgingsbrukerDTO {

    public String fodselsnr;
    public String formidlingsgruppekode;
    public ZonedDateTime iserv_fra_dato;
    public String nav_kontor;
    public String kvalifiseringsgruppekode;
    public String rettighetsgruppekode;
    public String hovedmaalkode;
    public String sikkerhetstiltak_type_kode;
    public String fr_kode;
    public Boolean har_oppfolgingssak;
    public Boolean sperret_ansatt;
    public Boolean er_doed;
    public ZonedDateTime doed_fra_dato;
}

