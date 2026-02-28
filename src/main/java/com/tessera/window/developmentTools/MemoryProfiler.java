package com.tessera.window.developmentTools;

/**
 * This entire class is static
 */
public class MemoryProfiler {

    /**
     * @return the intervalMS
     */
    public static long getIntervalMS() {
        return intervalMS;
    }

    /**
     * @param aIntervalMS the intervalMS to set
     */
    public static void setIntervalMS(long aIntervalMS) {
        intervalMS = aIntervalMS;
    }

    private final static Runtime rt = Runtime.getRuntime();

    public static float getMemoryUsagePercent() {
        return (float) (rt.totalMemory() - rt.freeMemory()) / rt.totalMemory();
    }

    public static long getMemoryUsed() {
        return rt.totalMemory() - rt.freeMemory();
    }

    public static long getMaxMemory() {
        return rt.maxMemory();
    }

    public static long getTotalMemory() {
        return rt.totalMemory();
    }

    /**
     * @return the memoryIncrease
     */
    public static float getByteIncreasePerSecond() {
        return byteIncreasePerSecond;
    }

    public static String getMemoryUsageAsString() {
        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        return String.format("Inc: %s/s  Used: %s  Total: %s  (%s%%)",
                formatBytes((long) getByteIncreasePerSecond()),
                formatBytes(total - free),
                formatBytes(total),
                Math.round(getMemoryUsagePercent() * 100));
    }

    private static long lastMemory;
    private static long lastTime;
    private static float byteIncreasePerSecond = 0;
    private static long intervalMS = 1000;

    /**
     * Just like Rate = Distance/Time, bytes/second increase = memory difference
     * in Bytes / time difference in seconds
     *
     * We cannot just calculate the memory difference because that would not
     * tell us how fast the memory usage is changing over time. The memory
     * difference is just the absolute amount of memory that has been used or
     * freed between two points in time. It does not take into account how long
     * it took to use or free that amount of memory. For example, if the memory
     * difference is 100 bytes, it could mean that the program used 100 bytes in
     * 1 second, or in 10 seconds, or in 100 seconds. The memory difference
     * alone does not tell us anything about the speed or efficiency of the
     * program.
     *
     * Dividing by the time difference gives us the memory increase per second,
     * which is a measure of how fast the memory usage is changing over time.
     * The memory increase per second is the rate of change of memory usage with
     * respect to time. It tells us how many bytes of memory are being used or
     * freed per second by the program. For example, if the memory difference is
     * 100 bytes and the time difference is 10 seconds, then the memory increase
     * per second is 10 bytes per second. This means that the program is using
     * or freeing 10 bytes of memory every second. The memory increase per
     * second gives us a better idea of the performance and behavior of the
     * program.
     */
    public static void update() {
        if (System.currentTimeMillis() - lastTime > getIntervalMS()) {
            //Calculate byte memory increase per second
            long memoryDiffInBytes = (getMemoryUsed() - lastMemory);
            float timeDiffInSeconds = (float) (System.currentTimeMillis() - lastTime) / 1000;
            byteIncreasePerSecond = memoryDiffInBytes / timeDiffInSeconds; //X bytes/2s = Y bytes/s

            lastMemory = getMemoryUsed();
            lastTime = System.currentTimeMillis();
        }
    }

    public static String formatBytes(long bytes) {
        if (bytes > 1024 * 1024 * 1024) {
            return String.format("%.2f", ((float) ((float) ((float) bytes / 1024) / 1024) / 1024)) + "gb";
        } else if (bytes > 1024 * 1024) {
            return String.format("%.1f", ((float) ((float) bytes / 1024) / 1024)) + "mb";
        } else if (bytes > 1024) {
            return String.format("%.1f", ((float) bytes / 1024)) + "kb";
        } else {
            return bytes + "b";
        }
    }
}
