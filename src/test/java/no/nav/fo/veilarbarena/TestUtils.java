package no.nav.fo.veilarbarena;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.nav.fo.feed.common.FeedElement;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    @Test
    public void test() {
        TestObject testObject = new TestObject("id", "value");
        FeedElement<TestObject> feedElement = Utils.toFeedElement(TestObject::getId).apply(testObject);

        assertThat(feedElement.getId()).isEqualTo("id");
        assertThat(feedElement.getElement()).isEqualTo(testObject);
    }

    @AllArgsConstructor
    @Getter
    class TestObject implements Comparable<TestObject> {
        String id;
        String value;

        @Override
        public int compareTo(TestObject o) {
            return 0;
        }
    }
}
