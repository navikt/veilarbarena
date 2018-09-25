package no.nav.fo.veilarbarena.api;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class UserDTO {

    public String aktoerid;
    public String fodselsnr;
    public String formidlingsgruppekode;
    public ZonedDateTime iserv_fra_dato;

}
