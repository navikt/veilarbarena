package no.nav.fo.veilarbarena.feed;

import no.nav.fo.feed.common.FeedElement;
import no.nav.fo.veilarbarena.domain.Iserv28;
import no.nav.fo.veilarbarena.service.Iserv28Service;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Iserv28ProviderTest {

    public static final String TIMESTAMP = "2018-07-24T08:38:55Z";

    @Test
    public void skal_parse_timestamp_og_videresende_fetchSize() {
        ArgumentCaptor<ZonedDateTime> timestamp = ArgumentCaptor.forClass(ZonedDateTime.class);
        ArgumentCaptor<Integer> fetchSize = ArgumentCaptor.forClass(Integer.class);
        Iserv28Service service = mock(Iserv28Service.class);
        when(service.finnBrukereMedIservI28Dager(timestamp.capture(), fetchSize.capture())).thenReturn(emptyList());

        Iserv28Provider provider = new Iserv28Provider(service);
        provider.fetchData(TIMESTAMP, 100);


        assertThat(timestamp.getValue()).isEqualTo(ZonedDateTime.of(2018, 7, 24, 8, 38, 55, 0, ZoneId.of("UTC")));
        assertThat(fetchSize.getValue()).isEqualTo(100);
    }

    @Test
    public void skal_mappe_om_til_feedelements() {
        ZonedDateTime now = ZonedDateTime.now();
        Iserv28Service service = mock(Iserv28Service.class);
        when(service.finnBrukereMedIservI28Dager(any(), anyInt())).thenReturn(asList(
                new Iserv28("akt1", now, now),
                new Iserv28("akt2", now.plusHours(1), now)
        ));

        Iserv28Provider provider = new Iserv28Provider(service);

        List<FeedElement<Iserv28>> feedElements = provider.fetchData(TIMESTAMP, 100)
                .collect(toList());

        Set<String> ider = feedElements.stream()
                .map(FeedElement::getId)
                .collect(toSet());

        assertThat(feedElements.size()).isEqualTo(2);
        assertThat(ider.size()).isEqualTo(2);
    }
}
