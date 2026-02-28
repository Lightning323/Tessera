import com.xbuilders.engine.common.world.chunk.Chunk;

public class IntBufferTester {

    public final static float maxMult12bits = (float) ((Math.pow(2, 12) / (Chunk.WIDTH)) - 6);

    /**
     *
     *
     * leftTopSun: 15 leftTopTorch: 0
     * leftTop: -16
     * leftTopSun2: -1 leftTopTorch2: 0
     *
     * leftTopSun: 11 leftTopTorch: 0
     * leftTop: -80
     * leftTopSun2: 11 leftTopTorch2: 0
     *
     * @param args
     */

    public static void main(String[] args) {
// Assume value1 and value2 are in the range 0-15 (fit in 4 bits)
        byte value1 = 15; // for example
        byte value2 = 15;  // for example

// Pack value1 and value2 into one byte
//        byte packedValue = (byte) ((value1 << 4) | value2);
        byte packedValue = (byte) (-80);

// Unpack the values from the packed byte
        byte unpackedValue1 = (byte) ((packedValue >> 4) & 0xF); // Extracts the first 4 bits
        byte unpackedValue2 = (byte) (packedValue & 0xF);        // Extracts the last 4 bits

// Print the packed and unpacked values
        System.out.println("Packed Value: " + packedValue);
        System.out.println("Unpacked Value 1: " + unpackedValue1);
        System.out.println("Unpacked Value 2: " + unpackedValue2);


        //        System.out.println("2^12 = " + Math.pow(2, 12));
//
//        for (float val = -1; val < 34; val += 0.5f) {
//            int converted = (int) ((val + 1) * maxMult12bits);
//            float unconverted = ((float) (converted) / maxMult12bits) - 1;
//
//            System.out.println(val + "\t  unconv: " + unconverted + "\t  is over: " + (converted >= Math.pow(2, 12)));
//        }


//        System.out.println("Hello World!");
//        ResizableIntArray ria = new ResizableIntArray(0);
//
//        for(int i = 0; i < 100; i++) {
//            ria.add(i);
//            System.out.println(ria.toString()+" \t capacity: "+ria.getArray().length);
//        }
//
//
////        ResizableIntBuffer rib = new ResizableIntBuffer(5);
////
////        for(int i = 0; i < 10000; i++) {
////            rib.add(i);
////            System.out.println(rib.toString()+" \t capacity: "+rib.getBuffer().capacity());
////        }

    }
}
