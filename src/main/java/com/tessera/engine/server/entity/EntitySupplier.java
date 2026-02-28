/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.engine.server.entity;


import com.tessera.engine.common.WorldCoord;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author zipCoder933
 */
public class EntitySupplier {
    public final String id;
    private final Supplier2 supplier;
    public Supplier<Float> spawnLikelyhood = () -> 0.00001f;


    @FunctionalInterface
    public interface DespawnCallback {
        boolean despawn(Entity e);
    }

    public WorldCoord spawnCondition;
    public DespawnCallback despawnCondition;
    public boolean isAutonomous;

    public EntitySupplier(String id, Supplier2 supplier) {
        this.id = id;
        this.supplier = Objects.requireNonNull(supplier);
        //Init the supplier
        spawnCondition = (x, y, z) -> false;
        despawnCondition = (e) -> true;
        supplier.get(0).initSupplier(this);
    }

    public Entity get(long uniqueIdentifier) {
        Entity e = supplier.get(uniqueIdentifier);
        e.supplier = this;
        return e;
    }

    @FunctionalInterface
    public static interface Supplier2 {
        /**
         * Gets a result.
         *
         * @return a result
         */
        Entity get(long uniqueIdentifier);
    }
}