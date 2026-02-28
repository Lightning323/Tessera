package com.xbuilders.utils.bytes;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SimpleKyro {
    public void writeByteArrayShort(Output out, byte[] bytes) {
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    public void writeByteArray(Output out, byte[] bytes) {
        out.writeInt((byte) bytes.length);
        out.write(bytes);
    }

    public byte[] readByteArrayShort(Input in) {
        int length = in.readShort();
        byte[] bytes = new byte[length];
        in.read(bytes);
        return bytes;
    }

    public byte[] readByteArray(Input in) {
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.read(bytes);
        return bytes;
    }
}
