package no.nav.veilarbarena.service.dto;

import no.nav.veilarbarena.controller.response.OppfolgingssakDTO;

public class ArenaOppfolgingssakDTO {
    String saksId;

    public OppfolgingssakDTO toOppfolgingssakDTO() {
        OppfolgingssakDTO dto = new OppfolgingssakDTO();
        dto.setOppfolgingssakId(saksId);
        return dto;
    }
}
