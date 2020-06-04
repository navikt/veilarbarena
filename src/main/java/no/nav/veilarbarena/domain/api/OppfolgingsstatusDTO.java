package no.nav.veilarbarena.domain.api;

import com.google.gson.annotations.SerializedName;
import lombok.Value;

import java.time.LocalDate;

@Value
public class OppfolgingsstatusDTO {

    @SerializedName("rettighetsgruppeKode")
    String rettighetsgruppe;

    @SerializedName("formidlingsgruppeKode")
    String formidlingsgruppe;

    @SerializedName("servicegruppeKode")
    String servicegruppe;

    @SerializedName("navOppfoelgingsenhet")
    String oppfolgingsenhet;

    @SerializedName("inaktiveringsdato")
    LocalDate inaktiveringsdato;

    @SerializedName("kanEnkeltReaktiveres")
    Boolean kanEnkeltReaktiveres;

}
