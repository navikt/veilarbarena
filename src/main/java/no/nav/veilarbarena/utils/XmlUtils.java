package no.nav.veilarbarena.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

import java.io.IOException;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class XmlUtils {

    private static final XmlMapper xmlMapper;

    static {
        JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);

        xmlMapper = new XmlMapper(module);
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SneakyThrows
    public static <T> T fromXml(String xml, Class<T> clazz) {
        return xmlMapper.readValue(xml, clazz);
    }

    public static class ArenaFloatDeserializer extends JsonDeserializer<Float> {

        @Override
        public Float deserialize(JsonParser parser, DeserializationContext context)
                throws IOException {
            String floatString = parser.getText();
            floatString = floatString.replace(",", ".");
            return Float.valueOf(floatString);
        }
    }
}
