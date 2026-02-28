package com.tessera.engine.common.json.gson;

import com.google.gson.*;
import com.tessera.engine.server.block.construction.BlockTexture;

import java.lang.reflect.Type;

public class BlockTextureTypeAdapter implements JsonSerializer<BlockTexture>, JsonDeserializer<BlockTexture> {

    @Override
    public JsonElement serialize(BlockTexture blockTexture, Type type, JsonSerializationContext jsonSerializationContext) {
        if (blockTexture == null) return null;

        //Add TOP as json array
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("pos_y", blockTexture.POS_Y_NAME);
        jsonObject.addProperty("pos_x", blockTexture.POS_X_NAME);
        jsonObject.addProperty("pos_z", blockTexture.POS_Z_NAME);
        jsonObject.addProperty("neg_z", blockTexture.NEG_Z_NAME);
        jsonObject.addProperty("neg_x", blockTexture.NEG_X_NAME);
        jsonObject.addProperty("neg_y", blockTexture.NEG_Y_NAME);
        return jsonObject;
    }

    @Override
    public BlockTexture deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String pos_y = jsonObject.get("pos_y").getAsString();
        String pos_x = jsonObject.get("pos_x").getAsString();
        String pos_z = jsonObject.get("pos_z").getAsString();
        String neg_z = jsonObject.get("neg_z").getAsString();
        String neg_x = jsonObject.get("neg_x").getAsString();
        String neg_y = jsonObject.get("neg_y").getAsString();
        BlockTexture blockTexture = new BlockTexture(pos_y, neg_y, pos_x, neg_x, pos_z, neg_z);
        return blockTexture;
    }


}
