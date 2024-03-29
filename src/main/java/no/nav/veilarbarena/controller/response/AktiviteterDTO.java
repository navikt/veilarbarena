package no.nav.veilarbarena.controller.response;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

@Data
@Accessors(chain = true)
public class AktiviteterDTO {
    List<Tiltaksaktivitet> tiltaksaktiviteter;
    List<Gruppeaktivitet> gruppeaktiviteter;
    List<Utdanningsaktivitet> utdanningsaktiviteter;

    @Data
    public static class Tiltaksaktivitet {
        String tiltaksnavn;
        String aktivitetId;
        String tiltakLokaltNavn;
        String arrangor;
        String bedriftsnummer;
        DeltakelsesPeriode deltakelsePeriode;
        Integer deltakelseProsent;
        String deltakerStatus;
        LocalDate statusSistEndret;
        String begrunnelseInnsoking;
        Float antallDagerPerUke;

        @Data
        @Accessors(chain = true)
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
        @Accessors(chain = true)
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
        List<Moteplan> moteplanListe = List.of();

        @Data
        @Accessors(chain = true)
        public static class Moteplan {
            LocalDate startDato;
            String startKlokkeslett; // f.eks: 13:00:00
            LocalDate sluttDato;
            String sluttKlokkeslett; // f.eks: 14:00:00
            String sted;
        }
    }

}
