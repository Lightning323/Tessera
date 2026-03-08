package fasterXML.json.custom;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fasterXML.json.jsonObject;


import java.io.IOException;

public class RecordDeserializer extends StdDeserializer<jsonObject> {

    public RecordDeserializer() {
        this(null);
    }

    public RecordDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public jsonObject deserialize(JsonParser parser, DeserializationContext ctx)
            throws IOException, JacksonException {

        JsonNode node = parser.getCodec().readTree(parser);

        int id = node.get("value").asInt()*2;
        String name = node.get("name").asText();

        jsonObject obj = new jsonObject();
        obj.setName(name);
        obj.setValue(id);
        return obj;
    }
}