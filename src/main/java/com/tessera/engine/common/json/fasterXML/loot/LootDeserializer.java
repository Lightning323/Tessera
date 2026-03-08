package com.tessera.engine.common.json.fasterXML.loot;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.tessera.engine.server.loot.output.Loot;

import java.io.IOException;

public class LootDeserializer extends StdDeserializer<Loot> {

    public LootDeserializer() {
        this(Loot.class);
    }

    public LootDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Loot deserialize(JsonParser parser, DeserializationContext ctx)
            throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);

        String itemID = node.get("item").asText();
        float chance = (float) node.get("chance").asDouble();
        int maxItems = node.get("maxItems").asInt();

        Loot obj = new Loot(itemID, chance, maxItems);

        return obj;
    }
}