package com.tessera.content.vanilla.entities.animal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.engine.client.ClientWindow;

import java.io.IOException;

public class Rabbit extends StaticLandAnimal {
    long lastJumpTime = 0;


    public Rabbit( ClientWindow window, long uniqueIdentifier) {
        super(uniqueIdentifier, window);
    }

    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);
        goForwardCallback = (amount) -> {
            if (amount > 0.01) {
                if (System.currentTimeMillis() - lastJumpTime > 500) {
                    lastJumpTime = System.currentTimeMillis();
                    pos.jump();
                }
            }
        };
    }

    static StaticLandAnimal_StaticData ead;

    @Override
    public StaticLandAnimal_StaticData getStaticData() throws IOException {
        if (ead == null) {
            ead = new StaticLandAnimal_StaticData(
                    "assets/tessera/entities/animal\\rabbit\\body.obj",
                    "assets/tessera/entities/animal\\rabbit\\textures");
            body = ead.body;
            textures = ead.textures;
        }
        return ead;
    }


}
