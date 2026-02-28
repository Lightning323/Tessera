package com.tessera.engine.common.json.fasterXML.itemStack;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.server.item.Item;
import com.tessera.engine.server.item.ItemStack;

import java.io.IOException;

public class ItemStackDeserializer extends StdDeserializer<ItemStack> {


    public ItemStackDeserializer() {
        this(ItemStack.class);
    }

    public ItemStackDeserializer(Class<?> vc) {
        super(vc);
    }

    //https://github.com/FasterXML/jackson-databind/issues/4461
    @Override
    public ItemStack deserialize(JsonParser parser,
                                 DeserializationContext ctx)
            throws IOException, JacksonException {
        JsonNode node = parser.getCodec().readTree(parser);
//        final JsonNode node = ctx.readTree(parser);
        String itemID = node.get("item").asText();
        Item item = Registrys.items.getItem(itemID); //We get the item from the registry
        ItemStack obj = new ItemStack(item); //We create the ItemStack
        obj.durability = (float) node.get("durability").asInt();
        obj.stackSize = node.get("stackSize").asInt();

        if (node.has("data")) {
            obj.nbtData = node.get("data").asText().getBytes();
        }
        return obj;
    }
}