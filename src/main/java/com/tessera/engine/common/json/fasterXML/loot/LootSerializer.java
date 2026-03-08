package com.tessera.engine.common.json.fasterXML.loot;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tessera.engine.server.loot.output.Loot;

import java.io.IOException;

public class LootSerializer extends StdSerializer<Loot> {

    public LootSerializer() {
        super(Loot.class);
    }

    @Override
    public void serialize(Loot src, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {

        gen.writeStartObject();
        String itemId = src.item;

        gen.writeStringField("item", itemId);
        gen.writeNumberField("chance", src.chance);
        gen.writeNumberField("maxItems", src.maxItems);
        gen.writeEndObject();

    }
}