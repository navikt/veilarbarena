package no.nav.veilarbarena.service.dto;

import no.nav.veilarbarena.domain.api.OppfolgingssakDTO;

public class ArenaOppfolgingssakDTO {
    String saksId;

    public OppfolgingssakDTO toOppfolgingssakDTO() {
        OppfolgingssakDTO dto = new OppfolgingssakDTO();
        dto.setOppfolgingssakId(saksId);
        return dto;
    }
}
