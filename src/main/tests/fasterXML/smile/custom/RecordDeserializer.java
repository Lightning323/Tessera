package fasterXML.smile.custom;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import fasterXML.smile.smileObject;

import java.io.IOException;

public class RecordDeserializer extends StdDeserializer<smileObject> {

    public RecordDeserializer() {
        this(null);
    }

    public RecordDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public smileObject deserialize(JsonParser p, DeserializationContext ctx)
            throws IOException, JacksonException {


//        // Custom deserialization logic
//        String name = null;
//        int value = 0;
//
//        // Parsing through the JSON fields
//        while (!p.isClosed()) {
//            String fieldName = p.getCurrentName();
//            p.nextToken();
//
//            if ("customName".equals(fieldName)) {
//                name = p.getValueAsString();  // Extract the custom name
//            } else if ("customValue".equals(fieldName)) {
//                value = p.getValueAsInt() / 2;  // Custom deserialization logic, dividing the value by 2
//            }
//            p.nextToken();
//        }
//
//        smileObject obj = new smileObject();
//        obj.setName(name);
//        obj.setValue(value);


        JsonNode node = p.getCodec().readTree(p);

        int id = node.get("value").asInt() * 2;
        String name = node.get("name").asText();

        smileObject obj = new smileObject();
        obj.setName(name);
        obj.setValue(id);

        return obj;
    }
}