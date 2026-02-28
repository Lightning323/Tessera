import com.xbuilders.engine.common.world.chunk.ChunkVoxels;

public class NumberPackTester {

    public static void main(String[] args) {
        ChunkVoxels voxels = new ChunkVoxels(16, 16, 16);
//
//short voxel = 1234;
//        voxels.setSun(0,0,0,(byte)5);
//        voxels.setTorch(0,0,0,(byte)0,(byte)3);
//
//        System.out.println("Packing test");
//        byte packed = voxels.getPackedSunAndTorch(0,0,0);
//        int[] unpacked = voxels.unpack4BitValues(packed);
//        System.out.println("Unpacked: "+unpacked[0] + ", " + unpacked[1]);
//        System.out.println("Original: "+voxels.getSun(0,0,0) + ", " + voxels.getTorch(0,0,0));
//
//        System.out.println("\nMesh packing test");
//        int thisPlanePacked = (voxel << 8) | (packed & 0xFF);
//        //Unpack to test and print the results
//
//        int voxelUnpacked = thisPlanePacked >> 8;
//        byte sunUnpacked = (byte) (thisPlanePacked & 0xFF);
//        System.out.println("Voxel: " + voxelUnpacked+" original: "+voxel);
//        System.out.println("Light: " + sunUnpacked+" original: "+packed);
//        System.out.println("Light unpacked: " + voxels.unpack4BitValues(sunUnpacked)[0] + ", " + voxels.unpack4BitValues(sunUnpacked)[1]);


//        System.out.println("\nThird int test");
//        //The mistake was that the bits were not shifted correctly
//        //TTTTTTTT TTTTTTTT 00000000 SSSSTTTT
//
//        int sun = 15 & 0x0F;
//        int torch = 0 & 0x0F;
//        byte light2 = (byte)((sun << 4) | torch);
//        int thirdInt = VertexSet.packThirdInt(524, light2);
//        System.out.println("third int: " + intToString(thirdInt,8));
//
//        //Unpack it
//        // Extract the texture value (16 bits) by shifting right by 16 bits
//        int textureID = (thirdInt >> 16) & 0x0FFFF;
//
//
//        // Extract the next 4 bits as sunlight and the rest of the bits as torchlight
//        int packedSun = (thirdInt >> 4) & 0x0F;
//        int packedTorch = thirdInt & 0x0F;
//        System.out.println("Unpacked texture: " + textureID);
//        System.out.println("Unpacked sunlight: " + packedSun);
//        System.out.println("Unpacked torchlight: " + packedTorch);

//        byte channel1 = 5;
//        byte channel2 = 14;
//        byte channel3 = 1;
//        byte channel4 = 12;
//
//        short value = 0;
//        value = TorchChannelSet.setChannel1(value, channel1);
//        value = TorchChannelSet.setChannel2(value, channel2);
//        value = TorchChannelSet.setChannel3(value, channel3);
//        value = TorchChannelSet.setChannel4(value, channel4);
//
//        System.out.println("Value: " + value);
//        System.out.println("Channel 1: " + TorchChannelSet.getChannel1(value));
//        System.out.println("Channel 2: " + TorchChannelSet.getChannel2(value));
//        System.out.println("Channel 3: " + TorchChannelSet.getChannel3(value));
//        System.out.println("Channel 4: " + TorchChannelSet.getChannel4(value));


        for(int a = 0; a < 16; a++) {
            for(int b = 0; b < 16; b++) {
                System.out.println("\nin:  "+a + ", " + b);
                voxels.setSun(0, 0, 0, (byte) a);
                voxels.setTorch(0, 0, 0, (byte) b);

//                System.out.println("out: "+Integer.toBinaryString(voxels.getPackedSunAndTorch(0, 0, 0)));

                System.out.println("out: "+voxels.getSun(0, 0, 0) + ", " + voxels.getTorch(0, 0, 0));
            }
        }


    }

/* public int channelToFalloff(byte channel) {
      switch (channel) {
          case 1:
              return 2;
          case 2:
              return 3;
          case 3:
              return 8;
          default://0
              return 1;
      }
    }

    //CHANNEL UTILITIES
    final static int torchChannelMask = 0b1111; // 4-bit mask

    public static byte getChannel1(short value) {
        return (byte) (value & torchChannelMask);
    }

    public static byte getChannel2(short value) {
        return (byte) ((value >> 4) & torchChannelMask);
    }

    public static byte getChannel3(short value) {
        return (byte) ((value >> 8) & torchChannelMask);
    }

    public static byte getChannel4(short value) {
        return (byte) ((value >> 12) & torchChannelMask);
    }

    public static byte getCombinedChannel(short value) {
        int a = Math.max((value & torchChannelMask)
                , (value >> 4 & torchChannelMask));

        int b = Math.max((value >> 8 & torchChannelMask)
                , (value >> 12 & torchChannelMask));

        return (byte) Math.max(a, b);
    }

    public static short setChannel1(short value, byte channel) {
        return (short) ((value & ~torchChannelMask) | (channel & torchChannelMask));
    }

    public static short setChannel2(short value, byte channel) {
        return (short) ((value & ~(torchChannelMask << 4)) | ((channel & torchChannelMask) << 4));
    }

    public static short setChannel3(short value, byte channel) {
        return (short) ((value & ~(torchChannelMask << 8)) | ((channel & torchChannelMask) << 8));
    }

    public static short setChannel4(short value, byte channel) {
        return (short) ((value & ~(torchChannelMask << 12)) | ((channel & torchChannelMask) << 12));
    }*/


    /**
     * Converts an integer to a 32-bit binary string
     *
     * @param number    The number to convert
     * @param groupSize The number of bits in a group
     * @return The 32-bit long bit string
     */
    public static String intToString(int number, int groupSize) {
        StringBuilder result = new StringBuilder();

        for (int i = 31; i >= 0; i--) {
            int mask = 1 << i;
            result.append((number & mask) != 0 ? "1" : "0");

            if (i % groupSize == 0)
                result.append(" ");
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }

    public int[] unpack4BitValues(byte packedByte) {
        int high = (packedByte >> 4) & 0x0F; // Shift right to get the high 4 bits and mask
        int low = packedByte & 0x0F;          // Mask to get the low 4 bits

        return new int[]{high, low};
    }
}
