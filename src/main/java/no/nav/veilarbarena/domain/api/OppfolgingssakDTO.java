package no.nav.veilarbarena.domain.api;

import com.google.gson.annotations.SerializedName;

public class OppfolgingssakDTO {
    @SerializedName(value="oppfolgingssakId", alternate={"saksId"})
    String oppfolgingssakId;
}
