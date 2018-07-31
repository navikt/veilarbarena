package no.nav.fo.veilarbarena;

import lombok.extern.slf4j.Slf4j;
import no.nav.fo.feed.common.FeedElement;

import java.util.function.Function;

@Slf4j
public class Utils {
    public static <S extends Comparable<S>> Function<S, FeedElement<S>> toFeedElement(Function<S, String> idMapper) {
        return (S s) -> new FeedElement<S>()
                .setId(idMapper.apply(s))
                .setElement(s);
    }
}
