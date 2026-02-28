package fasterXML.smile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.xbuilders.engine.server.item.Item;
import com.xbuilders.engine.server.item.ItemStack;
import com.xbuilders.engine.common.json.fasterXML.itemStack.ItemStackDeserializer;
import com.xbuilders.engine.common.json.fasterXML.itemStack.ItemStackSerializer;
import fasterXML.smile.custom.RecordDeserializer;
import fasterXML.smile.custom.RecordSerializer;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class Main {


    public static void main(String[] args) {
        SmileFactory smileFactory = new SmileFactory();
        //set flags
        smileFactory.enable(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT);
        smileFactory.enable(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES);

        //create new object mapper based on factory
        ObjectMapper objectMapper = new ObjectMapper(smileFactory);
//        SmileMapper smileMapper = new SmileMapper(smileFactory); //You could also use this instead of ObjectMapper

        // Create a module to register custom serializer and deserializer
        SimpleModule module = new SimpleModule();
        module.addSerializer(smileObject.class, new RecordSerializer()); // Register the custom serializer
        module.addDeserializer(smileObject.class, new RecordDeserializer()); // Register the custom deserializer

        Item item = new Item("xbuilders:hat", "Hat");
        HashMap<String, Item> itemMap = new HashMap<>();
        itemMap.put("xbuilders:hat", item);

        module.addSerializer(ItemStack.class, new ItemStackSerializer()); // Register the custom serializer
        module.addDeserializer(ItemStack.class, new ItemStackDeserializer()); // Register the custom deserializer

        // Register the module with the ObjectMapper
        objectMapper.registerModule(module);

        // Create an example object
        smileObject myObject = new smileObject();
        myObject.setName("John");
        myObject.setValue(25);

        // Serialize the object to a JSON string
        try {
            System.out.println("Original Object: " + myObject);

            // Use ByteArrayOutputStream (byte-based)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Serialize the object to JSON using the ByteArrayOutputStream (byte-based)
            objectMapper.writeValue(byteArrayOutputStream, myObject);
            System.out.println("Serialized JSON: " + new String(byteArrayOutputStream.toByteArray()));

            // Deserialize the JSON string back into the object
            smileObject deserializedObject = objectMapper.readValue(byteArrayOutputStream.toByteArray(), smileObject.class);
            System.out.println("Deserialized Object: " + deserializedObject);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ItemStack[] itemStacks = new ItemStack[]{
                    new ItemStack(item, 14),
                    new ItemStack(item, 15),
                    new ItemStack(item, 16)
            };
            System.out.println("\n\nOriginal Object: " + Arrays.toString(itemStacks));

            // Use ByteArrayOutputStream (byte-based)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Serialize the object to JSON using the ByteArrayOutputStream (byte-based)
            objectMapper.writeValue(byteArrayOutputStream, itemStacks);
            System.out.println("Serialized JSON: " + new String(byteArrayOutputStream.toByteArray()));

            // Deserialize the JSON string back into the object
            ItemStack[] deserializedObject = objectMapper.readValue(byteArrayOutputStream.toByteArray(),
                    new TypeReference<ItemStack[]>() {
                    });
            System.out.println("Deserialized Object: " + Arrays.toString(deserializedObject));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}