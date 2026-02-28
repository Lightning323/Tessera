package com.tessera.content.vanilla.entities.animal.quadPedal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.tessera.engine.client.Client;
import com.tessera.engine.client.ClientWindow;
import com.tessera.engine.server.Registrys;
import com.tessera.engine.common.players.PositionLock;
import com.tessera.engine.client.visuals.gameScene.rendering.entity.EntityMesh;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.math.RandomUtils;
import com.tessera.content.vanilla.entities.animal.LegPair;
import com.tessera.content.vanilla.entities.animal.mobile.AnimalAction;
import com.tessera.content.vanilla.entities.animal.mobile.LandAnimal;
import com.tessera.utils.resource.ResourceLister;
import com.tessera.window.utils.obj.OBJLoader;
import com.tessera.window.utils.texture.TextureUtils;

import java.io.IOException;
import java.util.Objects;

import static com.tessera.Main.LOGGER;

public abstract class QuadPedalLandAnimal extends LandAnimal {
    //This animal specific
    int textureIndex;
    boolean isSaddled;
    public static final String JSON_SADDLED = "saddled";

    public QuadPedalLandAnimal(long uniqueIdentifier, ClientWindow window, boolean rideable) {
        super(uniqueIdentifier, window);
        aabb.setOffsetAndSize(1f, 1.5f, 1f, true);
        this.rideable = rideable;
        frustumSphereRadius = 2;
        currentAction = new AnimalAction(AnimalAction.ActionType.IDLE, 1000);
    }

    public static class QuadPedalLandAnimal_StaticData {
        public EntityMesh body, sittingBody, saddle;
        public int[] textures;
        public LegPair legs;

        public QuadPedalLandAnimal_StaticData(
                String bodyOBJ,
                String sittingBodyOBJ,
                String legOBJ,
                String saddleOBJ,
                String texturesDir) throws IOException {
            //Generate body
            body = new EntityMesh();
            body.loadFromOBJ(OBJLoader.loadModel(resourceLoader.getResourceAsStream(bodyOBJ)));

            //Generate sitting body
            if (sittingBodyOBJ != null) {
                sittingBody = new EntityMesh();
                sittingBody.loadFromOBJ(OBJLoader.loadModel(resourceLoader.getResourceAsStream(sittingBodyOBJ)));
            }

            //Generate legs
            EntityMesh legsModel = new EntityMesh();
            legsModel.loadFromOBJ(OBJLoader.loadModel(resourceLoader.getResourceAsStream(legOBJ)));
            legs = new LegPair(legsModel);

            //Generate saddle
            if (saddleOBJ != null) {
                saddle = new EntityMesh();
                saddle.loadFromOBJ(OBJLoader.loadModel(resourceLoader.getResourceAsStream(saddleOBJ)));
            }

            //Generate textures
            String[] textureFiles = ResourceLister.listDirectSubResources(texturesDir);
            textures = new int[textureFiles.length];
            for (int i = 0; i < textureFiles.length; i++) {
                textures[i] = Objects.requireNonNull(
                        TextureUtils.loadTextureFromResource(textureFiles[i], false)).id;
            }
        }
    }

    @Override
    public void serializeDefinitionData(JsonGenerator generator) throws IOException {
        generator.writeNumberField(JSON_SPECIES, textureIndex);
        generator.writeBooleanField(JSON_SADDLED, isSaddled);
    }


    @Override
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        super.loadDefinitionData(hasData, parser, node);

        goForwardCallback = amount -> {
            legMovement += amount;
        };

        //Get static data from subclass
        try {
            QuadPedalLandAnimal_StaticData ead = getStaticData();
            this.body = ead.body;
            this.sittingBody = ead.sittingBody;
            this.legs = ead.legs;
            this.saddle = ead.saddle;
            this.textures = ead.textures;
        } catch (IOException e) {
            LOGGER.info("Error", e);
        }

        if (hasData) {
            textureIndex = node.get(JSON_SPECIES).asInt();
            textureIndex = MathUtils.clamp(textureIndex, 0, this.textures.length - 1);

            if (node.has(JSON_SADDLED)) isSaddled = node.get(JSON_SADDLED).asBoolean();

        } else textureIndex = RandomUtils.random.nextInt(this.textures.length);

    }

    public abstract QuadPedalLandAnimal_StaticData getStaticData() throws IOException;

    //This speces
    EntityMesh body, sittingBody, saddle;
    public int[] textures;
    LegPair legs;
    boolean rideable = true;

    private float legMovement = 0;
    public float SCALE = 0.6f;
    protected float legXSpacing = 0.32f * SCALE;
    protected float legZSpacing = 0.9f * SCALE;
    protected float legYSpacing = -1.3f * SCALE;
    public final PositionLock lock = new PositionLock(this, -1);


    public void animal_move() {
        if (playerIsRidingThis()) {
            float rotSpeed = 0.5f;
            if (Client.userPlayer.forwardKeyPressed()) {
                goForward(0.2f, true);
                rotSpeed = 3;
                currentAction = new AnimalAction(AnimalAction.ActionType.IDLE, 1000);
            } else if (allowVoluntaryMovement()) super.animal_move();

            if (Client.userPlayer.leftKeyPressed()) {
                setRotationYDeg(getRotationYDeg() - rotSpeed);
            } else if (Client.userPlayer.rightKeyPressed()) {
                setRotationYDeg(getRotationYDeg() + rotSpeed);
            }
        } else if (allowVoluntaryMovement() && inFrustum) super.animal_move();
    }

    @Override
    public void animal_drawBody() {

        shader.bind();
        if (!isOld()) modelMatrix.scale(0.6f);
        modelMatrix.update();
        modelMatrix.sendToShader(shader.getID(), shader.uniform_modelMatrix);

        if (currentAction != null && currentAction.type == AnimalAction.ActionType.IDLE
                && sittingBody != null
                && currentAction.duration > 1000) {
            drawSitting();
        } else {
            drawBody();
            //Z is the directon of the horse
            legs.draw(modelMatrix, shader, legXSpacing, legYSpacing, legZSpacing, legMovement, textures[textureIndex]);
            legs.draw(modelMatrix, shader, legXSpacing, legYSpacing, -legZSpacing, legMovement, textures[textureIndex]);
        }
    }

    protected void drawBody() {
        body.draw(false, textures[textureIndex]);
        if (saddle != null && canRide()) saddle.draw(false, textures[textureIndex]);
    }

    protected void drawSitting() {
        sittingBody.draw(false, textures[textureIndex]);
    }

    private boolean canRide() {
        return tamed && rideable && isOld() && isSaddled;
    }

    private boolean canSit() {
        return sittingBody != null;
    }

    @Override
    public boolean run_ClickEvent() {
        if (!tamed) return false;

        if (canRide()) {
            Client.userPlayer.positionLock = lock;
        } else if (
                Client.userPlayer.holdingItem(Registrys.items.getItem("tessera:saddle"))) {
            Client.userPlayer.getSelectedItem().stackSize--;
            isSaddled = true;
        } else if (canSit()) {//Sit command
            if (currentAction.type == AnimalAction.ActionType.IDLE) {
                currentAction = new AnimalAction(AnimalAction.ActionType.OTHER, 10);
            } else {//Make the animal sit
                if (distToPlayer < 5) {
                    setRotationYDeg((float) Math.toDegrees(getYDirectionToPlayer()));
                }
                currentAction = new AnimalAction(AnimalAction.ActionType.IDLE, 10 * 1000);
            }
        }
        return true;
    }
}