package no.nav.fo.veilarbarena.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class Bruker implements Serializable {
    String aktoerid;
    String etternavn;
    String fornavn;
    String nav_kontor;
    String formidlingsgruppekode;
    ZonedDateTime iserv_fra_dato;
    String kvalifiseringsgruppekode;
    String rettighetsgruppekode;
    String hovedmaalkode;
    String sikkerhetstiltak_type_kode;
    String fr_kode;
    String har_oppfolgingssak;
    String sperret_ansatt;
    Boolean er_doed;
    ZonedDateTime doed_fra_dato;
    ZonedDateTime tidsstempel;

    // Må override for å kunne kjøre equals på ZonedDateTime
    // Serialisering av ZonedDateTime fjerner zoneId og legger på offset
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bruker bruker = (Bruker) o;
        return Objects.equals(aktoerid, bruker.aktoerid) &&
                Objects.equals(etternavn, bruker.etternavn) &&
                Objects.equals(fornavn, bruker.fornavn) &&
                Objects.equals(nav_kontor, bruker.nav_kontor) &&
                Objects.equals(formidlingsgruppekode, bruker.formidlingsgruppekode) &&
                iserv_fra_dato.isEqual(bruker.iserv_fra_dato) &&
                Objects.equals(kvalifiseringsgruppekode, bruker.kvalifiseringsgruppekode) &&
                Objects.equals(rettighetsgruppekode, bruker.rettighetsgruppekode) &&
                Objects.equals(hovedmaalkode, bruker.hovedmaalkode) &&
                Objects.equals(sikkerhetstiltak_type_kode, bruker.sikkerhetstiltak_type_kode) &&
                Objects.equals(fr_kode, bruker.fr_kode) &&
                Objects.equals(har_oppfolgingssak, bruker.har_oppfolgingssak) &&
                Objects.equals(sperret_ansatt, bruker.sperret_ansatt) &&
                Objects.equals(er_doed, bruker.er_doed) &&
                doed_fra_dato.isEqual(bruker.doed_fra_dato) &&
                tidsstempel.isEqual(bruker.tidsstempel);
    }

    @Override
    public int hashCode() {

        return Objects.hash(aktoerid, etternavn, fornavn, nav_kontor, formidlingsgruppekode, iserv_fra_dato, kvalifiseringsgruppekode, rettighetsgruppekode, hovedmaalkode, sikkerhetstiltak_type_kode, fr_kode, har_oppfolgingssak, sperret_ansatt, er_doed, doed_fra_dato, tidsstempel);
    }
}
