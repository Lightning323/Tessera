package fasterXML.smile.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import fasterXML.smile.smileObject;

import java.io.IOException;

public class RecordSerializer extends StdSerializer<smileObject> {

    public RecordSerializer() {
        super(smileObject.class);
    }

    @Override
    public void serialize(smileObject value, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("value", value.getValue());
        gen.writeStringField("name", value.getName());
        gen.writeEndObject();
    }
}