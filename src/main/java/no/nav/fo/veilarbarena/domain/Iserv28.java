package no.nav.fo.veilarbarena.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@ToString
public class Iserv28 implements Comparable<Iserv28> {
    public final String aktorId;
    public final ZonedDateTime sistOppdatert;
    public final ZonedDateTime iservSiden;

    @Override
    public int compareTo(Iserv28 other) {
        return this.sistOppdatert.compareTo(other.sistOppdatert);
    }
}
