package com.tessera.engine.common.world.gen;

import com.tessera.engine.common.option.OptionsList;
import com.tessera.engine.common.world.Terrain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Content registers {@link Terrain} implementations here; engine code resolves
 * them by the name stored in the world save. This replaces the old
 * {@code Game.terrainsList}, which forced engine classes ({@code World},
 * world-creation UI) to reach content through the {@code Main.game} singleton.
 *
 * <p>Registration normally happens in content {@code Game.setupClient} and
 * {@code Game.setupServer} (dedicated servers never run the client setup, so
 * both paths must register). Duplicate names are ignored: first registration
 * wins, which also makes repeated setup calls harmless.
 */
public final class TerrainRegistry {

    private static final Map<String, Terrain> REGISTRY = new LinkedHashMap<>();

    private TerrainRegistry() {
    }

    public static synchronized void register(Terrain terrain) {
        Objects.requireNonNull(terrain, "terrain");
        Objects.requireNonNull(terrain.name, "terrain.name");
        REGISTRY.putIfAbsent(terrain.name, terrain);
    }

    /** Registered terrain by save-file name, or null when unknown. */
    public static synchronized Terrain get(String name) {
        return REGISTRY.get(name);
    }

    /** Snapshot of registered terrains in registration order (for world-creation UI). */
    public static synchronized List<Terrain> list() {
        return new ArrayList<>(REGISTRY.values());
    }

    /** Clears all registrations. Intended for headless tests only. */
    public static synchronized void clear() {
        REGISTRY.clear();
    }

    /**
     * Resolves a terrain by save-file name and initializes it for the world.
     * Returns null (and logs) when no terrain with that name is registered.
     */
    public static Terrain initForWorld(String name, int seed, OptionsList options, int version) {
        Terrain terrain;
        synchronized (TerrainRegistry.class) {
            terrain = REGISTRY.get(name);
        }
        if (terrain == null) {
            com.tessera.Main.LOGGER.error("Terrain not found: \"" + name + "\"");
            return null;
        }
        terrain.initForWorld(seed, options, version);
        return terrain;
    }
}
