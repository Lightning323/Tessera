//package com.xbuilders.engine.game.model.items.recipes.crafting.json;
//
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.databind.SerializerProvider;
//import com.fasterxml.jackson.databind.ser.std.StdSerializer;
//import com.xbuilders.engine.game.model.items.item.ItemStack;
//import com.xbuilders.engine.game.model.items.recipes.crafting.CraftingRecipe;
//
//import java.io.IOException;
//
//public class CraftingRecipeSerializer extends StdSerializer<CraftingRecipe> {
//
//    public CraftingRecipeSerializer() {
//        super(CraftingRecipe.class);
//    }
//
//    @Override
//    public void serialize(CraftingRecipe src, JsonGenerator gen,
//                          SerializerProvider serializers) throws IOException {
//
//        gen.writeStartObject();
//
//        // Start the "input" array
//        gen.writeArrayFieldStart("input");
//
//        // Write rows
//        String row = (src.input[0] == null ? "none" : src.input[0]) + "    " +
//                (src.input[1] == null ? "none" : src.input[1]) + "    " +
//                (src.input[2] == null ? "none" : src.input[2]);
//        gen.writeString(row);
//
//        row = (src.input[3] == null ? "none" : src.input[3]) + "    " +
//                (src.input[4] == null ? "none" : src.input[4]) + "    " +
//                (src.input[5] == null ? "none" : src.input[5]);
//        gen.writeString(row);
//
//        row = (src.input[6] == null ? "none" : src.input[6]) + "    " +
//                (src.input[7] == null ? "none" : src.input[7]) + "    " +
//                (src.input[8] == null ? "none" : src.input[8]);
//        gen.writeString(row);
//
//        // End the "input" array
//        gen.writeEndArray();
//
//        // Write other fields
//        gen.writeStringField("output", src.output);
//        gen.writeNumberField("amount", src.amount);
//
//        gen.writeEndObject();
//    }
//}
