//package com.tessera.engine.game.model.items.recipes.crafting.json;
//
//import com.fasterxml.jackson.core.JacksonException;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.databind.DeserializationContext;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
//import com.tessera.engine.game.model.items.item.Item;
//import com.tessera.engine.game.model.items.item.ItemStack;
//import com.tessera.engine.game.model.items.recipes.crafting.CraftingRecipe;
//
//import java.io.IOException;
//import java.util.HashMap;
//
//public class CraftingRecipeDeserializer extends StdDeserializer<CraftingRecipe> {
//
//    public CraftingRecipeDeserializer() {
//        super(CraftingRecipe.class);
//    }
//
//    @Override
//    public CraftingRecipe deserialize(JsonParser parser, DeserializationContext context) throws IOException {
//
//        JsonNode rootNode = parser.getCodec().readTree(parser);
//
//        // Deserialize "input" array
//        JsonNode inputNode = rootNode.get("input");
//        String[] input = new String[9];
//        int index = 0;
//        for (JsonNode row : inputNode) {
//            String[] rowItems = row.asText().split(" ");
//            for (int i = 0; i < rowItems.length; i++) {
//                input[index++] = rowItems[i].isEmpty() ? null : rowItems[i];
//            }
//        }
//
//        // Deserialize "output" and "amount"
//        String output = rootNode.get("output").asText();
//        int amount = rootNode.get("amount").asInt();
//
//        return new CraftingRecipe(input, output, amount);
//    }
//}
