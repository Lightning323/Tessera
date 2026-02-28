/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.xbuilders.engine.server.entity;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator;
import com.xbuilders.Main;
import com.xbuilders.engine.client.Client;
import com.xbuilders.engine.client.visuals.gameScene.rendering.entity.EntityShader;
import com.xbuilders.engine.client.visuals.gameScene.rendering.entity.EntityShader_ArrayTexture;
import com.xbuilders.engine.server.item.ItemStack;
import com.xbuilders.engine.common.world.chunk.Chunk;
import com.xbuilders.engine.common.world.chunk.ChunkVoxels;
import com.xbuilders.engine.common.world.wcc.WCCf;
import com.xbuilders.engine.common.world.wcc.WCCi;
import com.xbuilders.engine.common.json.fasterXML.itemStack.ItemStackDeserializer;
import com.xbuilders.engine.common.json.fasterXML.itemStack.ItemStackSerializer;
import com.xbuilders.utils.resource.ResourceLoader;
import com.xbuilders.engine.common.worldInteraction.collision.EntityAABB;
import com.xbuilders.window.render.MVP;
import org.joml.Vector3f;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import static com.xbuilders.engine.common.json.JsonManager.SMILE_HEADER;

/**
 * @author zipCoder933
 */
public abstract class Entity {

    /**
     * We are choosing to have 1 shader for all allEntities
     * Just want to make sure we keep the uniforms down so we dont have to update so many every frame
     * https://stackoverflow.com/questions/69664014/should-every-object-have-its-own-shader
     */
    public static EntityShader shader;
    public static EntityShader_ArrayTexture arrayTextureShader;
    public boolean sendMultiplayer;
    public final static ResourceLoader resourceLoader = new ResourceLoader();

    public boolean playerIsRidingThis() {
        return Client.userPlayer.positionLock != null && Client.userPlayer.positionLock.entity == this;
    }

    private void getLightForPosition() {
        Chunk chunk = Main.getClient().world.getChunk(chunkPosition.chunk);
        byte light = (byte) 0b11110000;

        if (chunk != null) {
            light = chunk.voxels.getPackedLight((int) Math.floor(chunkPosition.chunkVoxel.x), (int) Math.floor(chunkPosition.chunkVoxel.y), (int) Math.floor(chunkPosition.chunkVoxel.z));

            for (int i = 1; i < 3; i++) { //Go up, if the block is in an opaque block
                if (light == 0) {
                    WCCi wcc = new WCCi();
                    wcc.set((int) Math.floor(worldPosition.x), (int) Math.floor(worldPosition.y - i), (int) Math.floor(worldPosition.z));
                    chunk = Main.getClient().world.getChunk(wcc.chunk);
                    if (chunk != null) {
                        light = chunk.voxels.getPackedLight(wcc.chunkVoxel.x, wcc.chunkVoxel.y, wcc.chunkVoxel.z);
                    }
                } else break;
            }
        }

        //Unpack light
        sunValue = (float) ChunkVoxels.getSun(light) / 15;
        torchValue = (float) ChunkVoxels.getTorch(light) / 15;
    }

    public float sunValue;
    public float torchValue;
    public boolean spawnedNaturally = false;
    protected byte[] loadBytes;
    public EntitySupplier link;
    protected final long uniqueIdentifier;

    public final static String JSON_SPAWNED_NATURALLY = "spawnedNaturally";


    //Position
    public EntityAABB aabb;
    public final WCCf chunkPosition;
    public final Vector3f worldPosition;
    private final Vector3f prevWorldPosition;//KEEP PRIVATE

    //Model view projection
    public final MVP modelMatrix = new MVP();

    private boolean destroyMode = false;
    Chunk chunk;
    public float frustumSphereRadius = 1; //Each entity has a sphere that is used for frustum culling. This defines its radius.
    protected boolean needsInitialization;
    public boolean inFrustum; //This value is automatically set by the frustum culling tester
    public float distToPlayer;
    private static final SecureRandom entityIdentifierGenerator = new SecureRandom();
    public EntitySupplier supplier;

    public String getId() {
        return supplier.id;
    }

    public Entity(long uniqueIdentifier) {
        sendMultiplayer = false;
        aabb = new EntityAABB();
        worldPosition = aabb.worldPosition;
        prevWorldPosition = new Vector3f();
        chunkPosition = new WCCf();
        needsInitialization = true;
        if (uniqueIdentifier == 0)
            this.uniqueIdentifier = entityIdentifierGenerator.nextLong(); //Auto generate the identifier
        else this.uniqueIdentifier = uniqueIdentifier;
    }


    /**
     * Private entity drawing method, used to do things before and after the entity is drawn
     */
    protected void hidden_drawEntity() {
        if (shader == null) {
            shader = new EntityShader();
        }
        if (arrayTextureShader == null) {
            arrayTextureShader = new EntityShader_ArrayTexture();
        }
        if (inFrustum) {
            modelMatrix.identity().translate(worldPosition);//Model matrix is already in world position
            shader.setSunAndTorch(sunValue, torchValue);
        }
        client_draw();
    }

    public final static Kryo kryo = new Kryo();
    public final static SmileFactory smileFactory = new SmileFactory();
    public final static ObjectMapper smileObjectMapper = new ObjectMapper(smileFactory);


    static {
        Entity.kryo.register(byte[].class);
        smileFactory.enable(SmileGenerator.Feature.ENCODE_BINARY_AS_7BIT);
        smileFactory.enable(SmileGenerator.Feature.CHECK_SHARED_STRING_VALUES);

        //Item stack serializer
        SimpleModule module = new SimpleModule();
        module.addSerializer(ItemStack.class, new ItemStackSerializer()); // Register the custom serializer
        module.addDeserializer(ItemStack.class, new ItemStackDeserializer()); // Register the custom deserializer
        smileObjectMapper.registerModule(module);
    }

    /**
     * Used as another layer of abstraction to write definition data of entity.
     * If the entity has no data, we return the loaded bytes
     */
    public final byte[] serializeDefinitionData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator generator = smileFactory.createGenerator(baos);
        generator.writeStartObject(); // Start root object
        serializeDefinitionData(generator);
        generator.writeEndObject(); // End root object
        generator.close();
        byte[] entityBytes = baos.toByteArray();


        //System.out.println("Entity bytes: " + Arrays.toString(entityBytes));
        //If the entity has no data, we return the loaded bytes
        if (entityBytes.length == 0) {
            return loadBytes;
        } else return entityBytes;
    }

    /**
     * Used as another layer of abstraction to write definition data of entity.
     *
     * @return
     */
    public final byte[] serializeStateData() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        serializeStateData(output, kryo);
        output.close();
        return baos.toByteArray();
    }

    protected final void hidden_initializeEntity() {
        try {
            getLightForPosition();

            JsonParser parser = null;
            JsonNode node = null;
            //If there is no load bytes, parser and node are null
            if (loadBytes != null && loadBytes.length > 0 && new String(loadBytes).startsWith(SMILE_HEADER)) {
                parser = smileFactory.createParser(loadBytes);
                node = parser.getCodec().readTree(parser);
//                System.out.println("Loading entity JSON: " + new String(loadBytes));
            }
            loadDefinitionData(parser != null, parser, node);
            if (parser != null) parser.close();

        } catch (Exception e) {
            destroy();
        }

        needsInitialization = false;
        updatePosition();

        //We have to send the entity after it has been initialized
//        if (sendMultiplayer) Main.getServer().server.addEntityChange(this, GameServer.ENTITY_CREATED, true);
    }

    /**
     * Load the definition data of the entity and initialize it
     * Definition data includes persistent, largely static attributes:
     * •	Species (e.g., Zombie, Skeleton).
     * •	UUID (unique identifier for the entity).
     * •	Name/Custom Name Tags.
     */
    public void loadDefinitionData(boolean hasData, JsonParser parser, JsonNode node) throws IOException {
        if (supplier.isAutonomous && hasData) {
            if (node.has(JSON_SPAWNED_NATURALLY)) {
                spawnedNaturally = node.get(JSON_SPAWNED_NATURALLY).asBoolean();
            }
        }
    }

    /**
     * Write the definition data of the entity
     * Definition data includes persistent, largely static attributes:
     * •	Species (e.g., Zombie, Skeleton).
     * •	UUID (unique identifier for the entity).
     * •	Name/Custom Name Tags.
     */
    public void serializeDefinitionData(JsonGenerator generator) throws IOException {
        if (supplier.isAutonomous) {
            generator.writeBooleanField(JSON_SPAWNED_NATURALLY, spawnedNaturally);
        }
    }

    /**
     * Load the state data of the entity
     * State data reflects dynamic, real-time changes, such as:
     * •	Position/Velocity/Rotation (where it is and how it’s moving).
     * •	Health.
     * •	Action states (e.g., attacking, sneaking, or swimming).
     * •	Equipment changes.
     */
    public void loadStateData(Input input, Kryo kryo) {
    }

    /**
     * Write the state data of the entity
     * State data reflects dynamic, real-time changes, such as:
     * •	Position/Velocity/Rotation (where it is and how it’s moving).
     * •	Health.
     * •	Action states (e.g., attacking, sneaking, or swimming).
     * •	Equipment changes.
     */
    public void serializeStateData(Output output, Kryo kryo) {
    }


    /**
     * This method will be called when a chunk is saved (or removed)
     */
    public final boolean updatePosition() {
        aabb.update(true);//IF the entity goes outside of a chunk, it will not be reassigned to another chunk and it will dissapear when moved too far
        chunkPosition.set(worldPosition);

        boolean hasMoved = !worldPosition.equals(prevWorldPosition);
        if (hasMoved) { //If the entity has moved
            getLightForPosition();
            prevWorldPosition.set(worldPosition);
            entityMoveEvent();
        }
        return hasMoved;
    }

    public void entityMoveEvent() {
    }

    public void markAsModifiedByUser() {
        chunk.markAsModified();
    }

    protected void hidden_entityOnChunkMeshChanged() {
        getLightForPosition();
    }

    public abstract void server_update();

    public abstract void client_draw();


    @Override
    public String toString() {
        return "Entity{id=" + Long.toHexString(getUniqueIdentifier()) + '}';
    }

    public void destroy() {
        destroyMode = true;
    }

    public boolean isDestroyMode() {
        return destroyMode;
    }

    /**
     * Used as a convenience method to initialize the entity supplier
     * Only called once
     *
     * @param entitySupplier
     */
    public void initSupplier(EntitySupplier entitySupplier) {
    }

    /**
     * @return if we want to permit the click event to continue
     */
    public boolean run_ClickEvent() {
        return false;
    }

    public long getUniqueIdentifier() {
        return uniqueIdentifier;
    }


}
