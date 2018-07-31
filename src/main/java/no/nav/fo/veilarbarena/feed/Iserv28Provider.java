package no.nav.fo.veilarbarena.feed;

import no.nav.fo.feed.common.FeedElement;
import no.nav.fo.feed.producer.FeedProvider;
import no.nav.fo.veilarbarena.domain.Iserv28;
import no.nav.fo.veilarbarena.service.Iserv28Service;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.stream.Stream;

import static no.nav.fo.veilarbarena.Utils.toFeedElement;

public class Iserv28Provider implements FeedProvider<Iserv28> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME.withZone(ZoneId.of("UTC"));
    private static final Function<Iserv28, FeedElement<Iserv28>> feedElementMapper = toFeedElement(iserv28 -> FORMATTER.format(iserv28.sistOppdatert));

    private Iserv28Service service;

    @Inject
    public Iserv28Provider(Iserv28Service service) {
        this.service = service;
    }

    @Override
    public Stream<FeedElement<Iserv28>> fetchData(String sistTidsstempelStr, int pageSize) {
        return service.finnBrukereMedIservI28Dager(ZonedDateTime.parse(sistTidsstempelStr, FORMATTER), pageSize)
                .stream()
                .map(feedElementMapper);
    }
}
