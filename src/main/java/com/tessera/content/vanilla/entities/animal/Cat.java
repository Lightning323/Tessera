package com.tessera.content.vanilla.entities.animal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.engine.client.ClientWindow;

import java.io.IOException;


public class Cat extends StaticLandAnimal {
    public Cat(long uniqueIdentifier, ClientWindow window) {
        super(uniqueIdentifier, window);
    }

    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);//Always call super!
        setActivity(0.9f);
    }


    static StaticLandAnimal_StaticData ead;

    @Override
    public StaticLandAnimal_StaticData getStaticData() throws IOException {
        if (ead == null) {
            ead = new StaticLandAnimal_StaticData(
                    "assets/tessera/entities/animal\\cat\\body.obj",
                    "assets/tessera/entities/animal\\cat\\textures");
            body = ead.body;
            textures = ead.textures;
        }
        return ead;
    }

}

