/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tessera.window.utils.preformance;

/**
 * @author zipCoder933
 */
public class Stopwatch {
    /**
     * Gets the elapsed nanoseconds.
     *
     * @return the elapsed nanoseconds
     */
    public long getElapsedNanoseconds() {
        return elapsedNanoseconds;
    }

    /**
     * Returns the elapsed microseconds.
     *
     * @return the elapsed microseconds
     */
    public long getElapsedMicroseconds() {
        return elapsedMicroseconds;
    }

    /**
     * Gets the elapsed minutes.
     *
     * @return the elapsed minutes
     */
    public int getElapsedMinutes() {
        return elapsedMinutes;
    }

    /**
     * gets the elapsed seconds.
     *
     * @return the elapsed seconds
     */
    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Retrieves the elapsed milliseconds.
     *
     * @return the elapsed milliseconds
     */
    public long getElapsedMilliseconds() {
        return elapsedMilliseconds;
    }

    long startTime = -1;

    private int elapsedMinutes;
    private int elapsedSeconds;
    private long elapsedNanoseconds;
    private long elapsedMicroseconds;
    private long elapsedMilliseconds;

    /**
     * Starts / restarts the stopwatch. After being started, the stopwatch will
     * continue ticking forever unless you start() it again, and then the
     * stopwatch will start ticking from the beginning.
     */
    public void start() {
        startTime = System.nanoTime(); //start the clock
    }

    /**
     * Calculate the elapsed time.
     */
    public void calculateElapsedTime() {
//        if (startTime == -1) {
//            throw new IllegalAccessError("Stopwatch has not been started yet.");
//        }

        //If we wanted the time as congruent time units, we would need to subtract the lower times from the higher times,
        //however we DON'T want to do that with these variables because these variables are SUPPOSED to INDEPENDENTLY
        //represent the total elapsed time.
        elapsedNanoseconds = (System.nanoTime() - startTime);
        elapsedMicroseconds = getElapsedNanoseconds() / 1000;
        elapsedMilliseconds = getElapsedMicroseconds() / 1000;
        elapsedSeconds = (int) (elapsedMilliseconds / 1000);
        elapsedMinutes = elapsedSeconds / 60;
    }



    @Override
    public String toString() {
        //Subtract the lower times from the higher times.
        int elapsedSeconds2 = elapsedSeconds - (elapsedMinutes * 60);
        long elapsedNanoseconds2 = elapsedNanoseconds - getElapsedMicroseconds() * 1000;
        long elapsedMicroseconds2 = elapsedMicroseconds - getElapsedMilliseconds() * 1000;
        long elapsedMilliseconds2 = elapsedMilliseconds - (elapsedSeconds * 1000);

        return elapsedMinutes + "m "
                + elapsedSeconds2 + "s "
                + elapsedMilliseconds2 + "ms "
                + elapsedMicroseconds2 + "µs "
                + elapsedNanoseconds2 + "ns";
    }

}
