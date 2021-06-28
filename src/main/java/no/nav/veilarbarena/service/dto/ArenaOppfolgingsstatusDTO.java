package no.nav.veilarbarena.service.dto;

import no.nav.veilarbarena.controller.response.OppfolgingsstatusDTO;

import java.time.LocalDate;

public class ArenaOppfolgingsstatusDTO {
    String rettighetsgruppeKode;
    String formidlingsgruppeKode;
    String servicegruppeKode;
    String navOppfoelgingsenhet;
    LocalDate inaktiveringsdato;
    Boolean kanEnkeltReaktiveres;

    public OppfolgingsstatusDTO toOppfolgingsstatusDTO() {
        OppfolgingsstatusDTO dto = new OppfolgingsstatusDTO();
        dto.setRettighetsgruppe(rettighetsgruppeKode);
        dto.setFormidlingsgruppe(formidlingsgruppeKode);
        dto.setServicegruppe(servicegruppeKode);
        dto.setOppfolgingsenhet(navOppfoelgingsenhet);
        dto.setInaktiveringsdato(inaktiveringsdato);
        dto.setKanEnkeltReaktiveres(kanEnkeltReaktiveres);
        return dto;
    }
}
