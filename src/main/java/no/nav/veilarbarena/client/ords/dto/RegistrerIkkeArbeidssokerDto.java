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
        if (resultat.contains("finnes ikke i Folkeregisteret")) {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.FNR_FINNES_IKKE);
        } else if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker kan reaktiveres forenklet")) {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.KAN_REAKTIVERES_FORENKLET);
            /* Arena skal snart bytte slik at BRUKER_ALLEREDE_ARBS og BRUKER_ALLEREDE_IARBS kommer med status-kode 200 */
        } else if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe ARBS")) {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.BRUKER_ALLEREDE_ARBS);
        } else if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe IARBS")) {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.BRUKER_ALLEREDE_IARBS);
        }else {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.UKJENT_FEIL);
        }
    }
    public static RegistrerIkkeArbeidssokerDto okResult(String resultat) {
        if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe ARBS")) {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.BRUKER_ALLEREDE_ARBS);
        } else if (resultat.contains("Eksisterende bruker er ikke oppdatert da bruker er registrert med formidlingsgruppe IARBS")) {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.BRUKER_ALLEREDE_IARBS);
        } else {
            return new RegistrerIkkeArbeidssokerDto(resultat, RESULTAT.OK_REGISTRERT_I_ARENA);
        }
    }
    private RegistrerIkkeArbeidssokerDto(String resultat, RESULTAT kode) {
        this.resultat = resultat;
        this.kode = kode;
    }
}
