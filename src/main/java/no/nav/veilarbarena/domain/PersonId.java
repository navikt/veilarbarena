package no.nav.veilarbarena.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.EqualsAndHashCode;

import java.io.IOException;

@EqualsAndHashCode
public abstract class PersonId {
    private final String id;
    private final String type;

    private PersonId(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public String get() {
        return id;
    }

    public static Fnr fnr(String fnr) {
        return new Fnr(fnr);
    }
    public static AktorId aktorId(String aktorId) {
        return new AktorId(aktorId);
    }

    @JsonDeserialize(using = PersonId.PersonIdDeserializer.class)
    public static class Fnr extends PersonId {
        private Fnr(String id) {
            super(id, "fnr");
        }
    }

    @JsonDeserialize(using = PersonId.PersonIdDeserializer.class)
    public static class AktorId extends PersonId {
        private AktorId(String id) {
            super(id, "aktorId");
        }
    }

    static class PersonIdDeserializer extends JsonDeserializer {
        @Override
        public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
            String id = ((TextNode) treeNode.get("id")).asText();
            String type = ((TextNode) treeNode.get("type")).asText();

            switch (type) {
                case "fnr": return PersonId.fnr(id);
                case "aktorId": return PersonId.aktorId(id);
            }

            return null;
        }
    }
}