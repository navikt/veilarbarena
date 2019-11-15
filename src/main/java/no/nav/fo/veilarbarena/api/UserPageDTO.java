package no.nav.fo.veilarbarena.api;

import lombok.Value;

import java.util.List;

@Value
public class UserPageDTO {
    int page;
    int pages_total;
    List<UserDTO> users;
}
