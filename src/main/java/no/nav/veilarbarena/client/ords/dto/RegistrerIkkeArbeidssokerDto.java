package no.nav.veilarbarena.client.ords.dto;

import lombok.Data;

@Data
public class RegistrerIkkeArbeidssokerDto {
    private final String resultat;
    private RESULTAT kode;
    public enum RESULTAT {
        OK_REGISTRERT_I_ARENA,
        FNR_FINNES_IKKE,
        KAN_REAKTIVERES_FORENKLET,
        BRUKER_ALLEREDE_ARBS,
        BRUKER_ALLEREDE_IARBS,
        UKJENT_FEIL
    }


    /*
    http status 200
        { "resultat": "Ny bruker ble registrert ok som IARBS" }
        { "resultat": "Eksisterende bruker ble oppdatert og registrert ok som IARBS" }
        { "resultat":"Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe ARBS" }
        { "resultat":"Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe IARBS" }
    http status 422
        { "resultat":"Fødselsnummer 22*******38 finnes ikke i Folkeregisteret" }
        { "resultat":"Eksisterende bruker er ikke oppdatert da bruker kan reaktiveres forenklet som arbeidssøker" }
    http status 400
        { "resultat":"Ugyldig eller ingen personident er angitt" }
     */
    public static RegistrerIkkeArbeidssokerDto errorResult(String resultat) {
        var result = new RegistrerIkkeArbeidssokerDto(resultat);
        if (resultat.contains("finnes ikke i Folkeregisteret")) {
            result.kode = RESULTAT.FNR_FINNES_IKKE;
        } else if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker kan reaktiveres forenklet")) {
            result.kode = RESULTAT.KAN_REAKTIVERES_FORENKLET;
        } else {
            result.kode = RESULTAT.UKJENT_FEIL;
        }
        return result;
    }
    public static RegistrerIkkeArbeidssokerDto okResult(String resultat) {
        var result = new RegistrerIkkeArbeidssokerDto(resultat);
        if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe ARBS")) {
            result.kode = RESULTAT.BRUKER_ALLEREDE_ARBS;
        } else if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe IARBS")) {
            result.kode = RESULTAT.BRUKER_ALLEREDE_IARBS;
        } else {
            result.kode = RESULTAT.OK_REGISTRERT_I_ARENA;
        }
        return result;
    }
    private RegistrerIkkeArbeidssokerDto(String resultat) {
        this.resultat = resultat;
            this.kode = RESULTAT.OK_REGISTRERT_I_ARENA;
    }
}
