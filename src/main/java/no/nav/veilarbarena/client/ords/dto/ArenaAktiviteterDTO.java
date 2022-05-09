package no.nav.veilarbarena.client.ords.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ArenaAktiviteterDTO {
    Response response;

    @Data
    public static class Response {
        List<Tiltaksaktivitet> tiltaksaktivitetListe;
        List<Gruppeaktivitet> gruppeaktivitetListe;
        List<Utdanningsaktivitet> utdanningsaktivitetListe;
    }

    @Data
    public static class Tiltaksaktivitet {
        String tiltaksnavn;
        String aktivitetId;
        String tiltakLokaltNavn;
        String arrangoer;
        String bedriftsnummer;
        DeltakelsesPeriode deltakelsePeriode;
        Integer deltakelseProsent;
        String deltakerStatus;
        LocalDate statusSistEndret;
        String begrunnelseInnsoeking;

        @Data
        public static class DeltakelsesPeriode {
           LocalDate fom;
           LocalDate tom;
        }
    }

    @Data
    public static class Utdanningsaktivitet {
        String aktivitetstype;
        String aktivitetId;
        String beskrivelse;
        AktivitetPeriode aktivitetPeriode;

        @Data
        public static class AktivitetPeriode {
            LocalDate fom;
            LocalDate tom;
        }
    }

    @Data
    public static class Gruppeaktivitet {
        String aktivitetstype;
        String aktivitetId;
        String beskrivelse;
        String status;
        MoteplanListe moeteplanListe;

        @Data
        public static class MoteplanListe {
            LocalDate startDato;
            String startKlokkeslett; // f.eks: 13:00:00
            LocalDate sluttDato;
            String sluttKlokkeslett; // f.eks: 14:00:00
            String sted;
        }
    }
}

