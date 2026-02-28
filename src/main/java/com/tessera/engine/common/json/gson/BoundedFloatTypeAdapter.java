package com.tessera.engine.common.json.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.tessera.engine.common.option.BoundedFloat;

import java.io.IOException;

public class BoundedFloatTypeAdapter extends TypeAdapter<BoundedFloat> {

    @Override
    public void write(JsonWriter out, BoundedFloat value) throws IOException {
        out.beginObject();
        out.name("value").value(value.value);
//        out.name("min").value(value.min);
//        out.name("max").value(value.max);
        out.endObject();
    }

    @Override
    public BoundedFloat read(JsonReader in) throws IOException {
        in.beginObject();
        double value = 0;
        int min = 0;
        int max = 0;
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("value")) {
                value = in.nextDouble();
            }
//            else if (name.equals("min")) {
//                min = in.nextInt();
//            } else if (name.equals("max")) {
//                max = in.nextInt();
//            }
            else {
                in.skipValue();
            }
        }
        in.endObject();
        return new BoundedFloat((float) value);
    }
}