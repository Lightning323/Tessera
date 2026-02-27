// 
// Decompiled by Procyon v0.5.36
// 
package com.xbuilders.engine.common.world;

import com.xbuilders.engine.server.Difficulty;
import com.xbuilders.engine.common.option.OptionsList;
import com.xbuilders.engine.server.GameMode;
import org.joml.Vector3f;

import java.nio.file.Files;
import java.io.IOException;

import com.google.gson.Gson;
import com.xbuilders.engine.common.json.JsonManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

import org.joml.Vector3i;

import static com.xbuilders.engine.common.utils.MiscUtils.formatTime;

public class WorldData {

    private File directory;
    final String INFO_FILENAME = "info.json";
    private String name;
    private static final Gson gson = JsonManager.gson_classes_adapter;
    public DataFile data;

    public File getChunkFile(Vector3i position) {
        return new File(this.directory.getAbsolutePath(), "chunk " + position.x + " " + position.y + " " + position.z);
    }

    public boolean isChunkFile(File file) {
        return (file.getName().startsWith("chunk"));
    }

    public Vector3i getPositionOfChunkFile(File file) {
        if (isChunkFile(file)) {
            String[] split = file.getName().split(" ");
            return new Vector3i(Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        }
        return null;
    }


    public File getDirectory() {
        return this.directory;
    }

    public String getName() {
        return this.name;
    }


    public Vector3f getSpawnPoint() {
        if (this.data.spawnX == -1.0) {
            return null;
        }
        return new Vector3f(
                this.data.spawnX,
                this.data.spawnY,
                this.data.spawnZ);
    }

    public void setSpawnPoint(Vector3f playerPos) {
        this.data.spawnX = (int) playerPos.x;
        this.data.spawnY = (int) playerPos.y;
        this.data.spawnZ = (int) playerPos.z;
    }

    public int getSize() {
        return this.data.size;
    }

    public long getLastSaved() {
        return this.data.lastSaved;
    }

    public String getTerrain() {
        return this.data.terrain;
    }

    public int getSeed() {
        return this.data.seed;
    }


    public WorldData() {
        data = new DataFile();
    }

    /**
     * Create a deep clone of the worldData
     *
     * @param other
     */
    public WorldData(WorldData other) {
        this.directory = other.directory;
        this.name = directory.getName();
        //Load directly from json
        this.data = gson.fromJson(other.toJson(), DataFile.class);
        data.initFields();
    }

    public void load(final File directory) throws IOException {
        if (!directory.exists()) {
            throw new IOException("Directory does not exist");
        }
        this.directory = directory;
        this.name = directory.getName();
        Path path = new File(directory, INFO_FILENAME).toPath().normalize();
        this.data = gson.fromJson(
                Files.readString(
                        path
                )
                , DataFile.class);
        data.initFields();
    }

    public String toJson() {
        return gson.toJson(data);
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public void save() throws IOException {
        if (!getDirectory().exists()) {
            getDirectory().mkdirs();
        }
        data.lastSaved = System.currentTimeMillis();
        String json = gson.toJson(data);
        Files.writeString(Paths.get(getDirectory() + "\\" + INFO_FILENAME), json);
    }

    public void makeNew(String name, int size, Terrain terrain, int seed) {
        this.name = name;
        this.data.size = size;
        this.data.terrain = terrain.name;
        this.data.terrainVersion = terrain.version;
        this.data.terrainOptions = terrain.getOptionsCopy();
        this.data.seed = seed == 0 ? (int) (Math.random() * Integer.MAX_VALUE) : seed;
        this.directory = WorldsHandler.worldFile(name);
    }

    public void makeNew(String name, String json) {
        this.data = gson.fromJson(json, DataFile.class);
        this.name = name;
        this.directory = WorldsHandler.worldFile(name);
    }

    public String getDetails() {
        try {
            return "Name: " + name + "\n"
                    + (data.isJoinedMultiplayerWorld ? "(Joined World)" : "") + "\n"
                    + "\nType: " + data.terrain
                    + "\nGame: " + data.gameMode.toString()
                    + "\nDifficulty: " + data.difficulty.toString()
                    + "\nSeed: " + data.seed
                    + "\n\nLast played:\n" + formatTime(getLastSaved());

        } catch (Exception ex) {
            return "Error getting world details";
        }
    }


    public class DataFile {
        public boolean isJoinedMultiplayerWorld;
        public int size;
        public float spawnX;
        public float spawnY;
        public float spawnZ;
        public int terrainVersion;
        public long lastSaved;
        public String terrain;
        public int seed;
        public GameMode gameMode;
        public Difficulty difficulty;
        public OptionsList terrainOptions;
        public boolean alwaysDayMode;
        public double dayTexturePan = 0;

        public DataFile() {
            this.spawnX = -1.0f;
            this.spawnY = -1.0f;
            this.spawnZ = -1.0f;
            this.terrainVersion = 0;
            terrainOptions = new OptionsList();
            isJoinedMultiplayerWorld = false;
            alwaysDayMode = false;
            gameMode = GameMode.FREEPLAY;
            difficulty = Difficulty.NORMAL;
        }

        public void initFields() {
            if (gameMode == null) gameMode = GameMode.ADVENTURE;
            if (difficulty == null) difficulty = Difficulty.NORMAL;
        }
    }

    public String toString() {
        return "WorldInfo:   name=" + name + "  json=" + toJson();
    }
}
