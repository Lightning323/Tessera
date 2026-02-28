package fasterXML.json.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fasterXML.json.jsonObject;

import java.io.IOException;

public class RecordSerializer extends StdSerializer<jsonObject> {

    public RecordSerializer() {
        super(jsonObject.class);
    }

    @Override
    public void serialize(jsonObject value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {

        gen.writeStartObject();
        gen.writeNumberField("value", value.getValue());
        gen.writeStringField("name", value.getName());
        gen.writeEndObject();
    }
}