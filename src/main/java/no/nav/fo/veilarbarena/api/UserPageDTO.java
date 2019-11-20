package no.nav.fo.veilarbarena.api;

import lombok.Value;

import java.util.List;

@Value
public class UserPageDTO {
    int page_number;
    int page_number_total;
    List<UserDTO> users;
}
