package com.tessera.engine.common.json.gson;


import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.tessera.engine.common.option.OptionsList;
import com.tessera.engine.common.option.BoundedFloat;
import com.tessera.engine.common.option.BoundedInt;

import java.io.IOException;
import java.util.Map;

public class OptionsListTypeAdapter extends TypeAdapter<OptionsList> {

    @Override
    public void write(JsonWriter out, OptionsList terrainOptions) throws IOException {
        out.beginObject();
        for (Map.Entry<String, Object> entry : terrainOptions.entrySet()) {
            String key = entry.getKey();
            Object option = entry.getValue(); // TerrainOptions stores Option objects

            out.name(key);
            out.beginObject();

            if (option instanceof BoundedInt) {
                out.name("type").value("BInt");
                out.name("value").value(((BoundedInt) option).value);
            } else if (option instanceof BoundedFloat) {
                out.name("type").value("BFloat");
                out.name("value").value(((BoundedFloat) option).value);
            } else if (option instanceof String) {
                out.name("type").value("String");
                out.name("value").value(((String) option));
            } else if (option instanceof Boolean) {
                out.name("type").value("Boolean");
                out.name("value").value((boolean) option);
            } else if (option instanceof Integer) {
                out.name("type").value("Integer");
                out.name("value").value((int) option);
            } else if (option instanceof Float) {
                out.name("type").value("Float");
                out.name("value").value((float) option);
            }

            out.endObject();
        }
        out.endObject();
    }

    @Override
    public OptionsList read(JsonReader in) throws IOException {
        OptionsList terrainOptions = new OptionsList();

        in.beginObject();
        while (in.hasNext()) {
            String key = in.nextName();
            in.beginObject();

            String type = null;
            Object value = null;

            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("type")) {
                    type = in.nextString();
                } else if (name.equals("value")) {
                    value = switch (type) {
                        case "BInt" -> new BoundedInt(in.nextInt());
                        case "BFloat" -> new BoundedFloat((float) in.nextDouble());
                        case "String" -> (in.nextString());
                        case "Boolean" -> (in.nextBoolean());
                        case "Integer" -> (in.nextInt());
                        case "Float" -> (in.nextDouble());
                        default -> value;
                    };
                }
            }

            in.endObject();
            if (value != null) {
                terrainOptions.put(key, value);
            }
        }
        in.endObject();

        return terrainOptions;
    }
}
