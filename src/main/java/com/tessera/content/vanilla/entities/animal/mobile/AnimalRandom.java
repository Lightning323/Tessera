/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.content.vanilla.entities.animal.mobile;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tessera.engine.common.math.FastNoise;
import com.tessera.engine.common.math.MathUtils;
import com.tessera.engine.common.math.CustomRandom;

/**
 * @author zipCoder933
 */
public class AnimalRandom {


    public int nextInt(int lowerBound, int upperBound) {
        return random.nextInt(upperBound - lowerBound) + lowerBound;
    }

    public float nextFloat(float lowerBound, float upperBound) {
        return random.nextFloat(upperBound - lowerBound) + lowerBound;
    }

    public long nextLong(long lowerBound, long upperBound) {
        return random.nextLong(upperBound - lowerBound) + lowerBound;
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public int nextInt() {
        return random.nextInt();
    }

    public int nextInt(int val) {
        return random.nextInt(val);
    }

    private FastNoise noise;
    private CustomRandom random;

    public AnimalRandom() {
        super();
        noiseIndex = 0;
        noise = new FastNoise();
        random = new CustomRandom();
    }

    public void setSeed(int seed) {
        noiseIndex = 0;
        noiseSeed = (seed);
        random.setSeed(seed);
        noise.SetSeed((seed));
    }


    private int noiseSeed;

    //These parameters represent the state of this entire class
    int noiseIndex;
    //random.getTrueSeed();

    public void writeState(Output output, Kryo kryo) {
        kryo.writeObject(output, (long) random.getTrueSeed().get());
        kryo.writeObject(output, noiseIndex);
    }

    public void readState(Input input, Kryo kryo) {
        long seed = kryo.readObject(input, long.class);
        random.getTrueSeed().set(seed);
        noiseIndex = kryo.readObject(input, int.class);
    }

    public float noise(float frequency) {
        noiseIndex++;
        return noise.GetValueFractal(noiseSeed, (noiseIndex * frequency) - noiseSeed);
    }

    public float noise(float frequency, float min, float max) {
        return MathUtils.map(noise(frequency), -1, 1, min, max);
    }


}
