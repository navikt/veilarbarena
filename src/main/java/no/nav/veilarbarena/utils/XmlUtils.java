package no.nav.veilarbarena.utils;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import lombok.SneakyThrows;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.dataformat.xml.XmlMapper;

import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class XmlUtils {

    private static final XmlMapper xmlMapper = XmlMapper.builder()
            .defaultUseWrapper(false)
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false)
            .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, true)
            .configure(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS, true)
            .build();


    @SneakyThrows
    public static <T> T fromXml(String xml, Class<T> clazz) {
        return xmlMapper.readValue(xml, clazz);
    }

    public static class ArenaFloatDeserializer extends ValueDeserializer<Float> {

        @Override
        public Float deserialize(JsonParser parser, DeserializationContext context) {
            String floatString = parser.getString();
            floatString = floatString.replace(",", ".");
            return Float.valueOf(floatString);
        }
    }
}
