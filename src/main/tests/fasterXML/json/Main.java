package fasterXML.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import fasterXML.json.custom.RecordDeserializer;
import fasterXML.json.custom.RecordSerializer;

public class Main {

    public static void main(String[] args) {
        // Create an instance of ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        // Create a module to register custom serializer and deserializer
        SimpleModule module = new SimpleModule();
        module.addSerializer(jsonObject.class, new RecordSerializer()); // Register the custom serializer
        module.addDeserializer(jsonObject.class, new RecordDeserializer()); // Register the custom deserializer

        // Register the module with the ObjectMapper
        objectMapper.registerModule(module);

        // Create an example object
        jsonObject myObject = new jsonObject();
        myObject.setName("John");
        myObject.setValue(25);

        // Serialize the object to a JSON string
        try {
            // Convert the object to JSON string
            String jsonString = objectMapper.writeValueAsString(myObject);
            System.out.println("Serialized JSON String: " + jsonString);

            // Deserialize the JSON string back into the object
            jsonObject deserializedObject = objectMapper.readValue(jsonString, jsonObject.class);
            System.out.println("Deserialized Object: " + deserializedObject);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}