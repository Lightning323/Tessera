package com.tessera.engine.common.json.fasterXML.itemStack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tessera.engine.server.item.ItemStack;

import java.io.IOException;

public class ItemStackSerializer extends StdSerializer<ItemStack> {

    public ItemStackSerializer() {
        super(ItemStack.class);
    }

    @Override
    public void serialize(ItemStack src, JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {

        gen.writeStartObject();
        gen.writeStringField("item", src.item.id);
        gen.writeNumberField("durability", (int) src.durability);
        gen.writeNumberField("stackSize", src.stackSize);
        if (src.nbtData != null) gen.writeStringField("data", new String(src.nbtData));
        gen.writeEndObject();

    }
}