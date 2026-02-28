package com.tessera.engine.common.json.gson;

import com.google.gson.*;
import com.tessera.engine.common.option.BoundedFloat;
import com.tessera.engine.common.option.BoundedInt;
import com.tessera.engine.common.option.Option;

import java.lang.reflect.Type;

public class OptionTypeAdapter implements JsonSerializer<Option>, JsonDeserializer<Option> {

    @Override
    public JsonElement serialize(Option src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject wrapper = new JsonObject();

        if (src.getBoundedFloat() != null) {
            wrapper.addProperty("type", "BoundedFloat");
            wrapper.add("value", context.serialize(src.getBoundedFloat()));
        } else if (src.getBoundedInt() != null) {
            wrapper.addProperty("type", "BoundedInt");
            wrapper.add("value", context.serialize(src.getBoundedInt()));
        } else if (src.getString() != null) {
            wrapper.addProperty("type", "String");
            wrapper.addProperty("value", src.getString());
        } else {
            wrapper.addProperty("type", "Boolean");
            wrapper.addProperty("value", src.getBoolean());
        }

        return wrapper;
    }

    @Override
    public Option deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String type = obj.get("type").getAsString();
        JsonElement value = obj.get("value");

        return switch (type) {
            case "BoundedFloat" -> {
                BoundedFloat boundedFloat = context.deserialize(value, BoundedFloat.class);
                yield new Option(boundedFloat);
            }
            case "BoundedInt" -> {
                BoundedInt boundedInt = context.deserialize(value, BoundedInt.class);
                yield new Option(boundedInt);
            }
            case "String" -> new Option(value.getAsString());
            case "Boolean" -> new Option(value.getAsBoolean());
            default -> throw new JsonParseException("Unknown type: " + type);
        };
    }


}
