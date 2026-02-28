package fasterXML;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SmileTest2 {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello world");
        SmileFactory smileFactory = new SmileFactory();
        //set flags
        smileFactory.enable(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT);
        smileFactory.enable(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES);
        //create new object mapper based on factory
        ObjectMapper objectMapper = new ObjectMapper(smileFactory);


        // Create a StringWriter to hold the JSON
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = smileFactory.createGenerator(baos);

        generator.writeStartObject(); // Start root object

        generator.writeStringField("custom_name", "John".toUpperCase());
        generator.writeNumberField("custom_age", 30 * 2);
        generator.writeStringField("city", "New York");
        generator.writeStringField("zip", "10001");

        generator.writeEndObject(); // End root object
        generator.close();

        // Print the JSON
        System.out.println(new String(baos.toByteArray()));

        // Step 2: Read Smile data
        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        JsonParser parser = smileFactory.createParser(inputStream);

        JsonNode node = parser.getCodec().readTree(parser);



        if (node.has("custom_age")) System.out.println("age " + node.get("custom_age").asInt());
        if (node.has("city")) System.out.println("city: " + node.get("city").asText());

        parser.close();

    }
}
