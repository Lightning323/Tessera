package com.xbuilders.utils.bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class ByteUtils {

    // <editor-fold defaultstate="collapsed" desc="long">
    public static void writeLong(OutputStream out, final long x) throws IOException {
        out.write((byte) (x));
        out.write((byte) (x >> 8));
        out.write((byte) (x >> 16));
        out.write((byte) (x >> 24));
        out.write((byte) (x >> 32));
        out.write((byte) (x >> 40));
        out.write((byte) (x >> 48));
        out.write((byte) (x >> 56));
    }

    public static byte[] longToBytes(long lng) {
        byte[] b = new byte[]{
                (byte) lng,
                (byte) (lng >> 8),
                (byte) (lng >> 16),
                (byte) (lng >> 24),
                (byte) (lng >> 32),
                (byte) (lng >> 40),
                (byte) (lng >> 48),
                (byte) (lng >> 56)};
        return b;
    }

    public static long bytesToLong(final byte[] b) {
        return ((long) b[7] << 56)
                | ((long) b[6] & 0xff) << 48
                | ((long) b[5] & 0xff) << 40
                | ((long) b[4] & 0xff) << 32
                | ((long) b[3] & 0xff) << 24
                | ((long) b[2] & 0xff) << 16
                | ((long) b[1] & 0xff) << 8
                | ((long) b[0] & 0xff);
    }

    public static long bytesToLong(final byte[] b, final AtomicInteger start) {
        int s = start.get();
        start.set(start.get() + 8);
        return ((long) b[s + 7] << 56)
                | ((long) b[s + 6] & 0xff) << 48
                | ((long) b[s + 5] & 0xff) << 40
                | ((long) b[s + 4] & 0xff) << 32
                | ((long) b[s + 3] & 0xff) << 24
                | ((long) b[s + 2] & 0xff) << 16
                | ((long) b[s + 1] & 0xff) << 8
                | ((long) b[s + 0] & 0xff);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="short">
    public static void writeShort(OutputStream out, final short x) throws IOException {
        out.write((byte) (x & 0xFF));
        out.write((byte) ((x >> 8) & 0xFF));
    }

    public static void writeUnsignedShort(OutputStream out, final int x) throws IOException {
        out.write((byte) (x & 0xFF));
        out.write((byte) ((x >> 8) & 0xFF));
    }

    public static byte[] shortToBytes(final int x) {
        final byte b1 = (byte) x;
        final byte b2 = (byte) (x >> 8);
        return new byte[]{b1, b2};
    }

    public static int bytesToShort(final byte b1, final byte b2) {
        return (b2 << 8 | (b1 & 0xFF));
    }



    // </editor-fold>

    //    <editor-fold defaultstate="collapsed" desc="int">
    public static void writeInt(OutputStream out, final int x) throws IOException {
        out.write((byte) (x & 0xFF));
        out.write((byte) ((x >> 8) & 0xFF));
        out.write((byte) ((x >> 16) & 0xFF));
        out.write((byte) ((x >> 24) & 0xFF));
    }

    public static byte[] intToBytes(int x) {
        final byte b1 = (byte) x;
        final byte b2 = (byte) (x >> 8);
        final byte b3 = (byte) (x >> 16);
        final byte b4 = (byte) (x >> 24);
        return new byte[]{b1, b2, b3, b4};
    }


    public static int bytesToInt(final byte b1, final byte b2, final byte b3, final byte b4) {
        return b4 << 24 | (b3 & 0xFF) << 16 | (b2 & 0xFF) << 8 | (b1 & 0xFF);
    }

    public static int bytesToInt(final byte[] b, final AtomicInteger start) {
        int s = start.get();
        start.set(start.get() + 4);
        return b[s + 3] << 24 | (b[s + 2] & 0xFF) << 16 | (b[s + 1] & 0xFF) << 8 | (b[s + 0] & 0xFF);
    }
    // </editor-fold>

    //    <editor-fold defaultstate="collapsed" desc="float">
    public static byte[] floatToBytes(float value) {
        int intBits = Float.floatToIntBits(value);
        return new byte[]{
                (byte) (intBits >> 24),
                (byte) (intBits >> 16),
                (byte) (intBits >> 8),
                (byte) (intBits)};
    }

    public static float bytesToFloat(byte b1, byte b2, byte b3, byte b4) {
        int intBits =
                b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF);
        return Float.intBitsToFloat(intBits);
    }

    public static float bytesToFloat(byte[] b, final AtomicInteger start) {
        int s = start.get();
        start.set(start.get() + 4);
        int intBits =
                b[s + 0] << 24 | (b[s + 1] & 0xFF) << 16 | (b[s + 2] & 0xFF) << 8 | (b[s + 3] & 0xFF);
        return Float.intBitsToFloat(intBits);
    }

    public static void writeFloat(ByteArrayOutputStream baos, float value) {
        int intBits = Float.floatToIntBits(value);
        baos.write((byte) (intBits >> 24));
        baos.write((byte) (intBits >> 16));
        baos.write((byte) (intBits >> 8));
        baos.write((byte) (intBits));
    }

    // </editor-fold>

}